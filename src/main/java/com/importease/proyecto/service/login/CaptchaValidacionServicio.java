package com.importease.proyecto.service.login;

import javax.servlet.http.HttpSession;

public class CaptchaValidacionServicio {
    private static final String CAPTCHA_SESSION_KEY = "captcha_answer";

    public boolean isValid(HttpSession session, String submittedCaptcha) {
        if (session == null) {
            return false;
        }

        String expectedCaptcha = (String) session.getAttribute(CAPTCHA_SESSION_KEY);
        session.removeAttribute(CAPTCHA_SESSION_KEY);

        return expectedCaptcha != null
                && !expectedCaptcha.isEmpty()
                && submittedCaptcha != null
                && submittedCaptcha.equalsIgnoreCase(expectedCaptcha);
    }
}
