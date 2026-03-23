package dev.dochia.cli.core.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.JaccardSimilarity;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.UnaryOperator;
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
public abstract class WordUtils {


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
                    "Forbidden", "ServiceUnavailable",
                    "SocketTimeout",
                    "PermissionDenied",
                    "InvalidToken",
                    "ReadTimeout",
                    "WriteTimeout",
                    "ConnectTimeout",
                    "RequestTimeout",
                    "ResponseTimeout",
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
     * Detects the casing of a string based on its format.
     *
     * @param sample The string to detect the casing of.
     * @return The detected casing as a string.
     */
    public static String detectCasingFromString(String sample) {
        if (sample.contains("_") && sample.equals(sample.toUpperCase(Locale.ROOT))) {
            return "UPPER_SNAKE_CASE";
        } else if (sample.contains("_") && sample.equals(sample.toLowerCase(Locale.ROOT))) {
            return "lower_snake_case";
        } else if (sample.contains("-")) {
            return "kebab-case";
        } else if (Character.isLowerCase(sample.charAt(0)) && sample.matches(".*[A-Z].*")) {
            return "camelCase";
        } else if (Character.isUpperCase(sample.charAt(0)) && sample.matches(".*[a-z].*")) {
            return "PascalCase";
        } else if (sample.equals(sample.toLowerCase(Locale.ROOT))) {
            return "lowercase";
        }
        return "UPPER_SNAKE_CASE"; // default
    }

    /**
     * Coverts a string to the detected casing convention.
     *
     * @param name             the string to convert
     * @param casingConvention the casing to convert to
     * @return the converted string
     */
    public static String convertToDetectedCasing(String name, String casingConvention) {
        return switch (casingConvention) {
            case "lower_snake_case" -> name.replaceAll("([a-z])([A-Z])", "$1_$2")
                    .replaceAll("([A-Z])([A-Z][a-z])", "$1_$2")
                    .toLowerCase(Locale.ROOT);
            case "kebab-case" -> name.replaceAll("([a-z])([A-Z])", "$1-$2")
                    .replaceAll("([A-Z])([A-Z][a-z])", "$1-$2")
                    .toLowerCase(Locale.ROOT);
            case "camelCase" -> Character.toLowerCase(name.charAt(0)) + name.substring(1);
            case "PascalCase" -> name;
            case "lowercase" -> name.toLowerCase(Locale.ROOT);
            default -> name.replaceAll("([a-z])([A-Z])", "$1_$2")
                    .replaceAll("([A-Z])([A-Z][a-z])", "$1_$2")
                    .toUpperCase(Locale.ROOT);
        };
    }
}
