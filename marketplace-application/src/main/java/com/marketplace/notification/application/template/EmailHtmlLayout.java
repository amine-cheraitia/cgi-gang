package com.marketplace.notification.application.template;

final class EmailHtmlLayout {

    private static final String BRAND_COLOR     = "#FF6B35";
    private static final String BRAND_COLOR_DARK = "#e05a27";
    private static final String BG_COLOR        = "#f4f4f5";
    private static final String TEXT_COLOR      = "#1f2937";
    private static final String MUTED_COLOR     = "#6b7280";

    private EmailHtmlLayout() {}

    static String wrap(String title, String contentHtml) {
        return wrap(title, contentHtml, null, null);
    }

    static String wrap(String title, String contentHtml, String ctaLabel, String ctaUrl) {
        String ctaBlock = "";
        if (ctaLabel != null && ctaUrl != null) {
            ctaBlock = """
                <div style="text-align:center;margin:32px 0;">
                  <a href="%s"
                     style="background-color:%s;color:#ffffff;padding:14px 28px;
                            border-radius:8px;text-decoration:none;font-weight:bold;
                            font-size:15px;display:inline-block;
                            transition:background-color .2s;">
                    %s
                  </a>
                </div>
                """.formatted(ctaUrl, BRAND_COLOR, escape(ctaLabel));
        }

        return """
            <!DOCTYPE html>
            <html lang="fr">
            <head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1.0"></head>
            <body style="margin:0;padding:0;background-color:%s;font-family:'Segoe UI',Arial,sans-serif;color:%s;">
              <table width="100%%" cellpadding="0" cellspacing="0" style="background-color:%s;padding:40px 16px;">
                <tr><td align="center">
                  <table width="600" cellpadding="0" cellspacing="0"
                         style="background:#ffffff;border-radius:12px;overflow:hidden;
                                box-shadow:0 4px 20px rgba(0,0,0,0.08);">

                    <!-- HEADER -->
                    <tr>
                      <td style="background-color:%s;padding:28px 40px;text-align:center;">
                        <span style="color:#ffffff;font-size:26px;font-weight:800;letter-spacing:-0.5px;">
                          Miam<span style="opacity:0.85">Campus</span>
                        </span>
                        <p style="color:rgba(255,255,255,0.85);margin:6px 0 0;font-size:13px;">
                          Marketplace de billets
                        </p>
                      </td>
                    </tr>

                    <!-- BODY -->
                    <tr>
                      <td style="padding:40px 40px 24px;">
                        <h1 style="margin:0 0 20px;font-size:22px;color:%s;font-weight:700;">
                          %s
                        </h1>
                        <div style="font-size:15px;line-height:1.7;color:%s;">
                          %s
                        </div>
                        %s
                      </td>
                    </tr>

                    <!-- FOOTER -->
                    <tr>
                      <td style="background-color:#f9fafb;padding:24px 40px;border-top:1px solid #e5e7eb;">
                        <p style="margin:0;font-size:12px;color:%s;text-align:center;">
                          © 2026 MiamCampus · Vous recevez cet email car vous avez un compte sur notre plateforme.<br/>
                          <a href="mailto:support@miamcampus.com"
                             style="color:%s;text-decoration:none;">Contacter le support</a>
                        </p>
                      </td>
                    </tr>

                  </table>
                </td></tr>
              </table>
            </body>
            </html>
            """.formatted(
                BG_COLOR, TEXT_COLOR, BG_COLOR,
                BRAND_COLOR,
                TEXT_COLOR, escape(title),
                TEXT_COLOR, contentHtml,
                ctaBlock,
                MUTED_COLOR, BRAND_COLOR
        );
    }

    static String badge(String label, String color) {
        return "<span style=\"background-color:" + color + ";color:#fff;padding:3px 10px;"
                + "border-radius:20px;font-size:12px;font-weight:600;\">" + escape(label) + "</span>";
    }

    static String infoRow(String label, String value) {
        return "<tr>"
                + "<td style=\"padding:8px 0;color:#6b7280;font-size:14px;width:160px;\">" + escape(label) + "</td>"
                + "<td style=\"padding:8px 0;font-weight:600;font-size:14px;\">" + escape(value) + "</td>"
                + "</tr>";
    }

    static String infoTable(String... rows) {
        return "<table cellpadding=\"0\" cellspacing=\"0\" width=\"100%%\" "
                + "style=\"border-top:1px solid #e5e7eb;margin-top:20px;\">"
                + String.join("", rows)
                + "</table>";
    }

    static String escape(String value) {
        if (value == null) return "";
        return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;");
    }
}
