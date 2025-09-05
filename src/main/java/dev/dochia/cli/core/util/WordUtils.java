package dev.dochia.cli.core.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.JaccardSimilarity;
import org.apache.commons.text.similarity.LevenshteinDistance;

import java.util.*;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * Helper class for words operations.
 */
public abstract class WordUtils {
    private static final double LEVENSHTEIN_THRESHOLD = 0.85;
    private static final double JACCARD_THRESHOLD = 0.7;
    private static final double PATTERN_MATCH_BONUS = 0.2;

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
     * Compares two error messages and returns true if they are similar enough.
     *
     * @param error1 the first error message
     * @param error2 the second error message
     * @return true if the error messages are similar, false otherwise
     */
    public static boolean areErrorsSimilar(String error1, String error2) {
        if (StringUtils.isBlank(error1) || StringUtils.isBlank(error2)) {
            return false;
        }

        int distance = LevenshteinDistance.getDefaultInstance().apply(error1, error2);
        int maxLength = Math.max(error1.length(), error2.length());
        double levenshteinSimilarity = 1.0 - ((double) distance / maxLength);

        JaccardSimilarity jaccardSimilarity = new JaccardSimilarity();
        double tokenSimilarity = jaccardSimilarity.apply(error1, error2);

        String pattern1 = normalizeErrorMessage(error1);
        String pattern2 = normalizeErrorMessage(error2);
        boolean samePattern = pattern1.equals(pattern2);

        double combinedScore = (levenshteinSimilarity + tokenSimilarity) / 2;
        if (samePattern) {
            combinedScore += PATTERN_MATCH_BONUS;
        }

        return combinedScore >= LEVENSHTEIN_THRESHOLD ||
                (tokenSimilarity >= JACCARD_THRESHOLD && samePattern);
    }

    static String normalizeErrorMessage(String message) {
        String normalized = message.replaceAll("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}", "TIMESTAMP");

        normalized = normalized.replaceAll("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}", "UUID");
        normalized = normalized.replaceAll("[0-9a-f]{32,}", "HASH");

        normalized = normalized.replaceAll("(file|https?|ftp)://[^\\s]+", "URL");
        normalized = normalized.replaceAll("/[\\w/.-]+", "PATH");
        normalized = normalized.replaceAll("\\b\\d+\\b", "NUM");

        return normalized;
    }
}
