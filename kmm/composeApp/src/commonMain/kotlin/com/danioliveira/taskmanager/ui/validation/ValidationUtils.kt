package com.danioliveira.taskmanager.ui.validation

/**
 * Utility object for common validation rules used across the application.
 */
object ValidationUtils {
    /**
     * Regular expression for validating email addresses.
     */
    private val EMAIL_REGEX = Regex(
        "[a-zA-Z0-9+._%\\-]{1,256}" +
                "@" +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                "(" +
                "\\." +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                ")+"
    )

    /**
     * Minimum required length for passwords.
     * Using 8 characters as the standard for better security.
     */
    const val MIN_PASSWORD_LENGTH = 8

    /**
     * Validates if the provided email is in a valid format.
     *
     * @param email The email to validate
     * @return true if the email is valid, false otherwise
     */
    fun isEmailValid(email: CharSequence): Boolean {
        return email.isNotEmpty() && email.toString().matches(EMAIL_REGEX)
    }

    /**
     * Validates if the provided password meets the minimum length requirement.
     *
     * @param password The password to validate
     * @return true if the password is valid, false otherwise
     */
    fun isPasswordValid(password: CharSequence): Boolean {
        return password.length >= MIN_PASSWORD_LENGTH
    }

    /**
     * Validates if the provided confirmation password matches the original password.
     *
     * @param password The original password
     * @param confirmPassword The confirmation password to validate
     * @return true if the passwords match, false otherwise
     */
    fun isConfirmPasswordValid(password: CharSequence, confirmPassword: CharSequence): Boolean {
        return password.toString() == confirmPassword.toString()
    }
}
