package com.vcasino.user.utils;

public class EmailTemplate {
    public static String buildEmailConfirmationTemplate(String redirectUrl) {
        return "<div style=\"width:100%;background-color:#fff;margin:0;padding:0;font-family:'Open Sans',sans-serif\">\n" +
                "    <table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" style=\"border-collapse:collapse;width:100%;min-width:100%;height:auto\">\n" +
                "        <tbody>\n" +
                "        <tr>\n" +
                "            <td width=\"100%\" valign=\"top\" bgcolor=\"#ffffff\" style=\"padding-top:20px\">\n" +
                "                <table width=\"580\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" bgcolor=\"#ffffff\" style=\"border-collapse:collapse;margin:0 auto\">\n" +
                "                    <tbody>\n" +
                "                    <tr>\n" +
                "                        <td style=\"font-size:13px;color:#282828;font-weight:400;text-align:left;font-family:'Open Sans',sans-serif;line-height:24px;vertical-align:top;padding:15px 8px 10px 8px\" bgcolor=\"#ffffff\">\n" +
                "                            <h1 style=\"font-weight:600;margin:15px 0 25px 0;text-align:center\">Welcome to VCasino</h1>\n" +
                "                            <p>You’re just one click away from getting started with VCasino. All you need to do is verify your email address to activate your VCasino account.</p>\n" +
                "                            <a href=\"" + redirectUrl + "\" style=\"padding:10px;width:300px;display:block;text-decoration:none;font-weight:700;font-size:14px;font-family:'Open Sans',sans-serif;color:#000000;background:#f6c046;border-radius:5px;line-height:17px;margin:0 auto;text-align:center\" target=\"_blank\">\n" +
                "                                Confirm My Email\n" +
                "                            </a>\n" +
                "                            <p>You’re receiving this email because you recently created a new VCasino account. If this wasn’t you, please ignore this email.</p>\n" +
                "                        </td>\n" +
                "                    </tr>\n" +
                "                    </tbody>\n" +
                "                </table>\n" +
                "            </td>\n" +
                "        </tr>\n" +
                "        </tbody>\n" +
                "    </table>\n" +
                "</div>";
    }
}
