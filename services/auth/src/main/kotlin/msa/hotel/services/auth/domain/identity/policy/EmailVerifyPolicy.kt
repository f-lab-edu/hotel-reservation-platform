package msa.hotel.services.auth.domain.identity.policy

object EmailVerifyPolicy {
    const val VERIFY_EMAIL_EXPIRATION_IN_HOURS = 1L

    fun mailContent(
        serverUrl: String,
        email: String,
        code: String,
    ) = "<!DOCTYPE html>\n" +
        "<html lang=\"ko\">\n" +
        "<head>\n" +
        "    <meta charset=\"UTF-8\">\n" +
        "    <title>회원가입 이메일 인증</title>\n" +
        "</head>\n" +
        "<body style=\"margin: 0; padding: 0; box-sizing: border-box; background-color: #f4f4f4;\">\n" +
        "    <div style=\"width: 100%; max-width: 600px; margin: 40px auto; padding: 40px; " +
        "background-color: #ffffff; border-radius: 8px; box-shadow: 0 4px 8px rgba(0,0,0,0.1); " +
        "font-family: 'Apple SD Gothic Neo', 'Malgun Gothic', sans-serif; text-align: center;\">\n" +
        "        <h1 style=\"color: #333333; font-size: 24px; font-weight: 600;\">[호텔 예약 플랫폼] 회원가입을 환영합니다!</h1>\n" +
        "        <p style=\"color: #555555; font-size: 16px; line-height: 1.6;\">\n" +
        "            회원가입을 완료하려면 아래 버튼을 클릭하여 이메일 주소를 인증해주세요.<br>이 버튼은 15분 동안만 유효합니다.\n" +
        "        </p>\n" +
        "        \n" +
        "        <a href=\"$serverUrl/identity/verify/confirm?email=$email&code=$code\" target=\"_blank\" " +
        "style=\"display: inline-block; background-color: #007bff; color: white; margin-top: 30px; padding: 15px 30px; " +
        "border-radius: 5px; font-size: 16px; font-weight: bold; text-decoration: none;\">\n" +
        "            이메일 인증하기\n" +
        "        </a>\n" +
        "        \n" +
        "        <p style=\"margin-top: 30px; color: #888888; font-size: 14px;\">\n" +
        "            만약 직접 요청하지 않으셨다면 이 메일을 무시하셔도 됩니다.\n" +
        "        </p>\n" +
        "    </div>\n" +
        "</body>\n" +
        "</html>"

    fun confirmResponse(email: String) =
        """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <title>이메일 인증 완료</title>
        </head>
        <body>
            <script>
                alert("$email 이메일 인증이 완료되었습니다. 이 창은 자동으로 닫힙니다.");
                window.close(); // 현재 창을 닫는 스크립트
                self.close(); // 일부 브라우저 호환성을 위한 코드
            </script>
            <p>인증이 완료되었습니다. 이 창을 닫아주세요.</p> 
        </body>
        </html>
        """.trimIndent()
}
