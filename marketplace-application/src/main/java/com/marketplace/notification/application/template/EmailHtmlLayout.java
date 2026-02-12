package com.marketplace.notification.application.template;

final class EmailHtmlLayout {
    private EmailHtmlLayout() {
    }

    static String wrap(String title, String contentHtml) {
        return """
            <html>
              <body style="font-family:Arial,sans-serif;color:#1f2937;">
                <div style="max-width:600px;margin:0 auto;padding:16px;">
                  <h2 style="margin:0 0 16px 0;">%s</h2>
                  <div style="line-height:1.5;">%s</div>
                  <hr style="margin:24px 0;border:none;border-top:1px solid #e5e7eb;" />
                  <p style="font-size:12px;color:#6b7280;">Marketplace Billets</p>
                </div>
              </body>
            </html>
            """.formatted(escape(title), contentHtml);
    }

    static String escape(String value) {
        return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;");
    }
}
