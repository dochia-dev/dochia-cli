package dev.dochia.cli.core.model;

import java.util.List;

/** Creates expected results with description and reason. */
public interface ResultFactory {

  /**
   * Creates a message and reason for the case when the received response is documented and response
   * body matches response schema.
   *
   * @param receivedResponseCode the HTTP response code received from the service
   * @return a Result to use in reports
   */
  static Result createExpectedResponse(String receivedResponseCode) {
    String message =
        "Response matches expected result. Response code [%s] is documented and response body matches the corresponding schema."
            .formatted(receivedResponseCode);
    String reason = Reason.ALL_GOOD.value();

    return new Result(message, reason);
  }

  /**
   * Creates a message and reason for the case when the received response is documented, BUT
   * response body doesn't match response schema.
   *
   * @param receivedResponseCode the HTTP response code received from the service
   * @return a Result to use in reports
   */
  static Result createNotMatchingResponseSchema(String receivedResponseCode) {
    String message =
        "Response does NOT match expected result. Response code [%s] is documented, but response body does NOT match the corresponding schema."
            .formatted(receivedResponseCode);
    String reason = Reason.NOT_MATCHING_RESPONSE_SCHEMA.value();

    return new Result(message, reason);
  }

  /**
   * Creates a Result indicating that the response content type does not match the contract.
   *
   * @param expected The list of expected content types.
   * @param actual The actual content type received in the response.
   * @return A Result indicating the mismatch in content types.
   */
  static Result createNotMatchingContentType(List<String> expected, String actual) {
    String message =
        "Response content type not matching the contract: expected %s, actual [%s]"
            .formatted(expected, actual);
    String reason = Reason.RESPONSE_CONTENT_TYPE_NOT_MATCHING.value();
    return new Result(message, reason);
  }

  /**
   * Creates a message and reason for the case when the received response code is 501.
   *
   * @return a Result to use in reports
   */
  static Result createNotImplemented() {
    return new Result(
        "Response HTTP code 501: you forgot to implement this functionality!",
        Reason.NOT_IMPLEMENTED.description());
  }

  /**
   * Creates a message and reason for the case when the received response code is 404.
   *
   * @return a Result to use in reports
   */
  static Result createNotFound() {
    return new Result(
        "Response HTTP code 404: you might need to provide business context using --refData or --urlParams",
        Reason.NOT_FOUND.value());
  }

  /**
   * Creates a message and reason when response time exceeds maximum.
   *
   * @param receivedResponseTime the received response time in ms
   * @param maxResponseTime the max response time
   * @return a Result to use in reporting
   */
  static Result createResponseTimeExceedsMax(long receivedResponseTime, long maxResponseTime) {
    String message =
        "Test case executed successfully, but response time exceeds --maxResponseTimeInMs: actual %d, max %d"
            .formatted(receivedResponseTime, maxResponseTime);
    String reason = Reason.RESPONSE_TIME_EXCEEDS_MAX.value();

    return new Result(message, reason);
  }

  /**
   * Creates am unexpected exception message and reason. Typically, as a last resort when cannot
   * determine other reasons.
   *
   * @param playbook the playbook name causing the exception
   * @param errorMessage the message of the exception
   * @return a Result to use in reporting
   */
  static Result createUnexpectedException(String playbook, String errorMessage) {
    String message = "Playbook [%s] failed due to [%s]".formatted(playbook, errorMessage);
    String reason = Reason.UNEXPECTED_EXCEPTION.value();

    return new Result(message, reason);
  }

  /**
   * Creates an error leaks detected message and reason. This happens when the response contains
   * error messages that are not expected.
   *
   * @param keywords the keywords detected in the response
   * @return a Result to use in reporting
   */
  static Result createErrorLeaksDetectedInResponse(List<String> keywords) {
    String message =
        "The following keywords were detected in the response which might suggest an error details leak: %s"
            .formatted(keywords);
    String reason = Reason.ERROR_LEAKS_DETECTED.value();

    return new Result(message, reason);
  }

  /**
   * Creates an unexpected behaviour message and reason. This is not caused by an abnormal
   * functioning of the application, but rather a response code that was not expected, nor
   * documented, nor known not to typically be documented.
   *
   * @param receivedResponseCode the http response code received from the service
   * @param expectedResponseCode the expected http response code
   * @return a Result to use in reporting
   */
  static Result createUnexpectedBehaviour(
      String receivedResponseCode, String expectedResponseCode) {
    String message =
        "Unexpected behaviour: expected %s, actual [%s]"
            .formatted(expectedResponseCode, receivedResponseCode);
    String reason = Reason.UNEXPECTED_BEHAVIOUR.value() + " %s".formatted(receivedResponseCode);

    return new Result(message, reason);
  }

  /**
   * Creates an unexpected response code message and reason. Usually caused by a mismatch between
   * what is documented and what the service is responding.
   *
   * @param receivedResponseCode the http response code received from the service
   * @param expectedResponseCode the expected http response code
   * @return a Result to use in reporting
   */
  static Result createUnexpectedResponseCode(
      String receivedResponseCode, String expectedResponseCode) {
    String message =
        "Response does NOT match expected result. Response code is NOT from a list of expected codes for this PLAYBOOK: expected %s, actual [%s]"
            .formatted(expectedResponseCode, receivedResponseCode);
    String reason =
        Reason.UNEXPECTED_RESPONSE_CODE.value() + ": %s".formatted(receivedResponseCode);

    return new Result(message, reason);
  }

  /**
   * Creates an undocumented response code message and reason. This happens when the returned
   * response code is not documented inside the contract.
   *
   * @param receivedResponseCode the received response code
   * @param expectedResponseCode the expected response code
   * @param documentedResponseCodes all documented response codes
   * @return a Result to use in reporting
   */
  static Result createUndocumentedResponseCode(
      String receivedResponseCode, String expectedResponseCode, String documentedResponseCodes) {
    String message =
        "Response does NOT match expected result. Response code is from a list of expected codes for this PLAYBOOK, but it is undocumented: expected %s, actual [%s], documented response codes: %s"
            .formatted(expectedResponseCode, receivedResponseCode, documentedResponseCodes);
    String reason =
        Reason.UNDOCUMENTED_RESPONSE_CODE.value() + ": %s".formatted(receivedResponseCode);

    return new Result(message, reason);
  }

  /**
   * Holds message and reason information when exceptional situations happen when running dochia.
   *
   * @param message the message result
   * @param reason a short description of the message that will be displayed in summary page
   */
  record Result(String message, String reason) {}

  enum Reason {
    ALL_GOOD("All Good!", "The response matches the expected result"),
    NOT_MATCHING_RESPONSE_SCHEMA(
        "Not matching response schema",
        "The response body does NOT match the corresponding schema defined in the OpenAPI contract"),
    NOT_IMPLEMENTED("Not implemented", "You forgot to implement this functionality!"),
    NOT_FOUND(
        "Not found", "You might need to provide business context using --refData or --urlParams"),
    RESPONSE_TIME_EXCEEDS_MAX(
        "Response time exceeds max",
        "The response time exceeds the maximum configured response time supplied using --maxResponseTimeInMs, default is 0 i.e no limit"),
    UNEXPECTED_EXCEPTION(
        "Unexpected exception",
        "An unexpected exception occurred. This might suggest an issue with dochia itself"),
    ERROR_LEAKS_DETECTED(
        "Error details leak",
        "The response contains error messages that might expose sensitive information"),
    UNEXPECTED_RESPONSE_CODE(
        "Unexpected response code",
        "The response code is documented inside the contract, but not expected for the current playbook"),
    UNDOCUMENTED_RESPONSE_CODE(
        "Undocumented response code",
        "The response code is expected for the current playbook, but not documented inside the contract"),
    RESPONSE_CONTENT_TYPE_NOT_MATCHING(
        "Response content type not matching the contract",
        "The response content type does not match the one defined in the OpenAPI contract"),
    UNEXPECTED_BEHAVIOUR(
        "Unexpected behaviour",
        "dochia run the test case successfully, but the response code was not expected, nor documented, nor known to typically be documented");

    private final String value;
    private final String description;

    Reason(String value, String description) {
      this.value = value;
      this.description = description;
    }

    public String description() {
      return description;
    }

    public String value() {
      return value;
    }
  }
}
