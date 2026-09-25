package com.zee.courseauthdemo.datatype;


/**
 * @dev : Ezekiel Eromosei
 * @date : 13 Jun, 2026
 */

public class ErrorCodeConstants {
    private ErrorCodeConstants() {}


    public static final String INCORRECT_USERNAME_PASSWORD = "AUTH-001";
    public static final String INVALID_GRANT_TYPE = "AUTH-002";
    public static final String INVALID_CLIENT = "AUTH-003";
    public static final String UNAUTHORIZED_CLIENT = "AUTH-004";
    public static final String USER_INACTIVE = "AUTH-005";
    public static final String USER_LOCKED = "AUTH-006";
    public static final String INVALID_SCOPE = "AUTH-007";
    public static final String INVALID_USER_SESSION = "AUTH-008";
    public static final String USER_NOT_FOUND = "AUTH-009";

    public static final String SOMETHING_WENT_WRONG = "AUTH-999";

    public static final String SERVER_ERROR = SOMETHING_WENT_WRONG;
    public static final String INVALID_TOKEN = SOMETHING_WENT_WRONG;
    public static final String INVALID_GRANT = SOMETHING_WENT_WRONG;
}
