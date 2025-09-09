package dev.dochia.cli.core.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.JaccardSimilarity;
import org.apache.commons.text.similarity.LevenshteinDistance;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * String similarity and normalization utilities.
 * <p>
 * Behavior:
 * <p>
 * - Precompiled regex patterns for performance.
 * <p>
 * - Normalization:
 * <li>collapses content inside escaped quotes: \"...\"  -> \"\"</li>
 * <li>masks ID-like, unquoted uppercase tokens (>=3 chars) to TOKEN (with a small whitelist),
 * UUID, hashes, timestamps, URLs, paths, digits, zero-width marks, etc.</li>
 * <li>areErrorsSimilar(...) uses a cheap Jaccard gate and a thresholded Levenshtein.</li>
 */
public final class WordUtils {


    // --- thresholds (keep aligned with your previous logic) ---
    public static final double COMBINED_THRESHOLD = 0.85d;
    public static final double JACCARD_THRESHOLD = 0.70d;  // token gate

    private static final JaccardSimilarity JS = new JaccardSimilarity();

    // --- caches ---
    private static final Map<String, String> NORMALIZED_CACHE = new ConcurrentHashMap<>(8192);

    // --- precompiled patterns (performance and clarity) ---
    private static final Pattern TS = Pattern.compile(
            "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(?:\\.\\d+)?(?:Z|[+\\-]\\d{2}:\\d{2})?");
    private static final Pattern UUID = Pattern.compile(
            "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}");
    private static final Pattern HASH = Pattern.compile("[0-9a-fA-F]{32,}");
    private static final Pattern URL = Pattern.compile("(?:file|https?|ftp)://\\S+");
    private static final Pattern PATH = Pattern.compile("(?:[A-Za-z]:)?(?:/|\\\\)[\\w .\\-/_]+");
    private static final Pattern DIGITS = Pattern.compile("\\b\\d+\\b");
    // Base64-like / URL-safe token chunks
    private static final Pattern BASE64ISH = Pattern.compile("\\b[A-Za-z0-9+/\\-_]{16,}={0,2}\\b");

    // Uppercase/ID-like tokens (≥3 chars) – replaced with TOKEN unless in whitelist
    private static final Pattern UPPER_TOKEN = Pattern.compile("\\b[A-Z][A-Z0-9_\\-]{2,}\\b");
    private static final Set<String> UPPER_WHITELIST = Set.of(
            "GET", "PUT", "POST", "PATCH", "DELETE", "HEAD", "OPTIONS",
            "TRUE", "FALSE", "NULL",
            "INFO", "WARN", "ERROR", "DEBUG",
            "HTTP", "HTTPS", "TLS", "SSL",
            "JSON", "XML", "CSV", "UTC"
    );

    private static final Set<String> ERROR_KEYWORDS =
            Set.of(
                    "StackTrace",
                    "BadRequest",
                    "InternalServerError",
                    "Unauthorized",
                    "Forbidden",
                    "ServiceUnavailable",
                    "Timeout",
                    "PermissionDenied",
                    "InvalidToken",
                    "MethodNotAllowed",
                    "ResourceNotFound",
                    "RateLimitExceeded",
                    "ClientError",
                    "ServerError",
                    "AuthenticationFailed",
                    "AuthenticationError",
                    "AuthorizationError",
                    "ConnectionTimeout",
                    "SSLHandshakeError",
                    "ConnectionRefused",
                    "MalformedRequest",
                    "MissingParameter",
                    "InvalidResponse",
                    "TransactionFailed",
                    "InvalidHeader",
                    "InvalidGrant",
                    "APIKeyError",
                    "DatabaseError",
                    "QueryTimeout",
                    "DataLossError",
                    "RequestEntityTooLarge",
                    "TooManyRequests",
                    "UnsupportedMediaType",
                    "UnprocessableEntity",
                    "DataIntegrityViolation",

                    // JavaScript/Node.js
                    "ReferenceError",
                    "SyntaxError",
                    "TypeError",
                    "RangeError",
                    "EvalError",
                    "UncaughtException",
                    "UnhandledRejection",
                    "ProcessError",
                    "HeapOverflow",
                    "TimeoutError",
                    "ENOTFOUND",
                    "ECONNREFUSED",
                    "EADDRINUSE",
                    "EPIPE",
                    "ETIMEDOUT",
                    "EPERM",
                    "ENETUNREACH",
                    "ECONNRESET",
                    "EEXIST",
                    "EISDIR",

                    // C#
                    "ArgumentNullException",
                    "InvalidOperationException",
                    "TaskCanceledException",
                    "FileLoadException",
                    "UnauthorizedAccessException",
                    "KeyNotFoundException",
                    "SecurityException",
                    "ArgumentOutOfRangeException",
                    "DirectoryNotFoundException",
                    "OperationCanceledException",
                    "StackOverflowException",
                    "FormatException",
                    "NotImplementedException",
                    "InvalidCastException",
                    "TimeoutException",
                    "OverflowException",
                    "DivideByZeroException",
                    "ObjectDisposedException",
                    "IndexOutOfRangeException",

                    // Python
                    "ValueError",
                    "KeyError",
                    "AttributeError",
                    "IndexError",
                    "ModuleNotFoundError",
                    "ZeroDivisionError",
                    "ImportError",
                    "IOError",
                    "RuntimeError",
                    "FileNotFoundError",
                    "StopIteration",
                    "MemoryError",
                    "FloatingPointError",
                    "ConnectionError",
                    "AssertionError",
                    "BrokenPipeError",
                    "PermissionError",

                    // Go (Golang)
                    "panic",
                    "runtime error",
                    "json: cannot unmarshal",
                    "unexpected end of JSON",
                    "InvalidArgumentError",
                    "NilPointerDereference",
                    "ChannelClosedError",
                    "DivideByZeroError",
                    "fatal error: stack overflow",
                    "index out of range",
                    "invalid memory address or nil pointer dereference",
                    "deadlock",

                    // Ruby
                    "NoMethodError",
                    "ArgumentError",
                    "LoadError",
                    "NameError",
                    "EOFError",
                    "StandardError",
                    "ThreadError",
                    "Timeout::Error",
                    "EncodingError",
                    "SystemExit",

                    // PHP
                    "FatalError",
                    "ParseError",
                    "Notice",
                    "DivisionByZeroError",
                    "MemoryLimitExceeded",
                    "PDOException",
                    "ErrorException",
                    "InvalidArgument",
                    "OutOfMemoryError",
                    "UnexpectedValueException",

                    // C++
                    "SegmentationFault",
                    "std::exception",
                    "std::runtime_error",
                    "std::invalid_argument",
                    "std::out_of_range",
                    "std::bad_alloc",
                    "MemoryLeak",
                    "StackOverflow",
                    "SIGSEGV",
                    "SIGABRT",
                    "std::length_error",
                    "std::overflow_error",
                    "std::underflow_error",
                    "std::domain_error",
                    "std::range_error",
                    "std::logic_error",

                    // Java
                    "NullPointerException",
                    "ArrayIndexOutOfBoundsException",
                    "StringIndexOutOfBoundsException",
                    "IllegalArgumentException",
                    "NumberFormatException",
                    "IllegalStateException",
                    "ConcurrentModificationException",
                    "FileNotFoundException",
                    "IOException",
                    "ClassCastException",
                    "UnsupportedOperationException",
                    "InterruptedException",
                    "SQLException",
                    "ClassNotFoundException",
                    "NoSuchMethodException",
                    "InvocationTargetException",
                    "InstantiationException",

                    // Kotlin
                    "NoSuchElementException",
                    "IndexOutOfBoundsException",
                    "TypeCastException",
                    "KotlinNullPointerException",
                    "KotlinIllegalArgumentException",

                    // Swift
                    "IndexOutOfRange",
                    "UnexpectedNil",
                    "TypeMismatch",
                    "OutOfBounds",
                    "UnwrapError",
                    "Segfault",
                    "DivideByZero",
                    "DecodingError",
                    "KeyDecodingError");

    // Remove Zs/Cs/marks and odd spaces
    private static final Pattern ZCMS = Pattern.compile("[\\p{Z}\\p{C}\\p{So}\\p{M}\\p{Sk}]+");
    private static final Pattern MULTI_SPACE = Pattern.compile("\\s{2,}");

    private static final List<String> DELIMITERS = List.of("", "-", "_");

    private WordUtils() {
        // ntd
    }

    /**
     * Starts from a list of words and creates all possible combinations matching all cases and
     * delimiters.
     *
     * @param words the list of words making up a given field name based on the identified casing
     * @return all possible combinations with different casing and delimiters
     */
    public static Set<String> createWordCombinations(String[] words) {
        Set<String> result = new TreeSet<>();

        for (String delimiter : DELIMITERS) {
            result.addAll(progressiveJoin(capitalizeFirstLetter(words), delimiter, String::valueOf));
            result.addAll(
                    progressiveJoin(capitalizeFirstLetter(words), delimiter, StringUtils::uncapitalize));
            result.addAll(progressiveJoin(words, delimiter, String::toLowerCase));
            result.addAll(progressiveJoin(words, delimiter, String::toUpperCase));
        }
        return result;
    }

    private static Set<String> progressiveJoin(
            String[] words, String delimiter, UnaryOperator<String> function) {
        Set<String> result = new TreeSet<>();

        for (int i = 0; i < words.length; i++) {
            result.add(String.join(delimiter, Arrays.copyOfRange(words, i, words.length)));
        }

        return result.stream().map(function).collect(Collectors.toSet());
    }

    private static String[] capitalizeFirstLetter(String[] words) {
        String[] result = new String[words.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = StringUtils.capitalize(words[i]);
        }

        return result;
    }

    /**
     * Returns the string representation of an object or {@code null} if the object is {@code null}.
     *
     * @param obj The object whose string representation is to be returned.
     * @return The string representation of the object, or {@code null} if the object is {@code null}.
     */
    public static String nullOrValueOf(Object obj) {
        return obj == null ? null : String.valueOf(obj);
    }

    /**
     * Checks if two strings match, disregarding case, by converting them to lowercase using the root
     * locale.
     *
     * @param string1 The first string for comparison.
     * @param string2 The second string for comparison.
     * @return {@code true} if the strings match (ignoring case), {@code false} otherwise.
     * @throws NullPointerException If 'string1' or 'string2' is null.
     */
    public static boolean matchesAsLowerCase(String string1, String string2) {
        return string2.toLowerCase(Locale.ROOT).matches(string1.toLowerCase(Locale.ROOT));
    }

    public static List<String> getKeywordsMatching(String response, Set<String> providedKeywords) {
        if (response == null) {
            return List.of();
        }
        Set<String> toCheck = providedKeywords.isEmpty() ? ERROR_KEYWORDS : providedKeywords;
        return toCheck.stream()
                .filter(
                        keyword -> response.toLowerCase(Locale.ROOT).contains(keyword.toLowerCase(Locale.ROOT)))
                .toList();
    }

    /**
     * Normalize an error message to a structural form:
     * - collapse \"...\" content to \"\"
     * - replace variable-like tokens with placeholders
     * - squash whitespace/noise
     *
     * @param s the error message to normalize
     * @return the error message normalized
     */
    public static String normalizeErrorMessage(String s) {
        if (StringUtils.isBlank(s)) {
            return "";
        }

        String r = s;

        // 1) Collapse escaped, inner quoted segments so only the quotes remain.
        r = collapseEscapedQuotedSegments(r);

        // 2) Replace common highly-variable substrings with placeholders.
        r = TS.matcher(r).replaceAll("TIMESTAMP");
        r = UUID.matcher(r).replaceAll("UUID");
        r = HASH.matcher(r).replaceAll("HASH");
        r = URL.matcher(r).replaceAll("URL");
        r = PATH.matcher(r).replaceAll("PATH");
        r = DIGITS.matcher(r).replaceAll("NUM");
        r = BASE64ISH.matcher(r).replaceAll("TOKEN");

        // 3) Replace uppercase ID-like tokens (generic, handles DYX/XIIR... cases).
        r = replaceUpperTokens(r);

        // 4) Remove zero-width and spacing noise, normalize whitespace.
        r = ZCMS.matcher(r).replaceAll(" ");
        r = MULTI_SPACE.matcher(r).replaceAll(" ").trim();

        return r;
    }

    // Helper: replace UPPER_TOKEN occurrences with TOKEN unless whitelisted
    private static String replaceUpperTokens(String s) {
        StringBuilder out = new StringBuilder(s.length());
        Matcher m = UPPER_TOKEN.matcher(s);
        while (m.find()) {
            String tok = m.group();
            if (UPPER_WHITELIST.contains(tok)) {
                m.appendReplacement(out, tok);
            } else {
                m.appendReplacement(out, "TOKEN");
            }
        }
        m.appendTail(out);
        return out.toString();
    }

    // Helper: collapse occurrences of \" ... \" to \"\"
    // (works well for messages like: ... parsing \"👩🏾false\" : invalid syntax)
    private static String collapseEscapedQuotedSegments(String s) {
        int i = 0;
        int n = s.length();
        StringBuilder sb = new StringBuilder(n);
        while (i < n) {
            int open = s.indexOf("\\\"", i);
            if (open < 0) {
                sb.append(s, i, n);
                break;
            }
            // copy up to the start of the escaped quote
            sb.append(s, i, open);
            // write collapsed pair \"\"
            sb.append("\\\"\\\"");
            // find the next closing escaped quote
            int j = open + 2;
            int close = s.indexOf("\\\"", j);
            if (close < 0) {
                // no closing pair; append rest and finish
                sb.append(s, j, n);
                break;
            }
            // skip the content and the closing pair
            i = close + 2;
        }
        return sb.toString();
    }

    /**
     * Main similarity predicate (stable and fast).
     * <p>
     * - Cheap Jaccard gate on normalized strings.
     * - Thresholded Levenshtein (banded) based on what remains necessary.
     *
     * @param a the first error message
     * @param b the second error message
     * @return true if the error messages are similar, false otherwise
     */
    public static boolean areErrorsSimilar(String a, String b) {
        if (StringUtils.isBlank(a) || StringUtils.isBlank(b)) {
            return false;
        }
        if (a.equals(b)) {
            return true;
        }

        // Normalize once with caching
        final String na = NORMALIZED_CACHE.computeIfAbsent(a, WordUtils::normalizeErrorMessage);
        final String nb = NORMALIZED_CACHE.computeIfAbsent(b, WordUtils::normalizeErrorMessage);

        // Fast structural equality
        if (na.equals(nb)) {
            return true;
        }

        // Cheap token similarity gate
        final double token = JS.apply(na, nb);
        if (token < JACCARD_THRESHOLD) {
            return false;
        }

        // Compute minimal LD similarity still needed to reach combined threshold.
        // combined = (ldSim + token) / 2 >= COMBINED_THRESHOLD
        final double minLdSim = Math.max(0.0, 2 * COMBINED_THRESHOLD - token);

        // Convert to an edit-distance bound over the normalized strings:
        final int maxLen = Math.max(na.length(), nb.length());
        final int maxEdits = (int) Math.ceil(maxLen * (1.0 - minLdSim));

        final Integer dist = new LevenshteinDistance(maxEdits).apply(na, nb);
        if (dist < 0) {
            return false; // exceeded bound
        }
        final double ldSim = 1.0 - (dist.doubleValue() / maxLen);

        return (ldSim + token) / 2.0 >= COMBINED_THRESHOLD;
    }
}
