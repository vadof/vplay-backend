package com.vcasino.user.utils;

public class RegexUtil {
    public static final String PASSWORD_REGEX = "[\\u0020-\\u007E]+";
    public static final String SPECIAL_SYMBOL_REGEX = "[!\"#$%&'()*+,\\-./:; <=>?@\\\\^_`{|}~\\]\\[]";
    public static final String USERNAME_REGEX = "[a-zA-Z0-9_]+";
    public static final String CAPITAL_LETTER_REGEX = "[A-Z]";
    public static final String NUMBER_REGEX = "[0-9]";
}
