package io.slingr.services.sample.services;


import io.slingr.services.utils.FilesUtils;
import io.slingr.services.utils.Json;
import org.apache.commons.lang.StringUtils;

import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Class that helps to build the web UI of the endpoint
 *
 * Created by lefunes on 02/12/16.
 */
public class HttpHelper {

    public enum Menu{
        HOME,
        WEATHER,
        DATA_STORE
    }

    public static String formatPage(String wsUrl, StringBuilder page, Menu menu) {
        return formatPage(wsUrl, page, null, menu);
    }

    public static String formatPage(String wsUrl, StringBuilder page, String js, Menu menu) {
        final StringBuilder sb = new StringBuilder();
        addHeader(wsUrl, sb, js, menu);
        sb.append(page);
        addFooter(sb);
        return sb.toString();
    }

    public static void addHeader(String wsUrl, StringBuilder sb, String js, Menu menu) {
        sb.append("<!DOCTYPE html>\n");
        sb.append("<html lang=\"en\">\n");
        sb.append("\t<head>\t\n");
        sb.append("\t\t<meta charset=\"utf-8\">\n");
        sb.append("\t\t<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n");
        sb.append("\t\t<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n");
        sb.append("\t\t<title>Sample endpoint</title>\n");
        sb.append("\t\t<link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css\" integrity=\"sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u\" crossorigin=\"anonymous\">\n");
        sb.append("\t\t<link rel=\"icon\" href=\"/favicon.ico\" type=\"image/x-icon\">\n");
        sb.append("\t\t<!--[if lt IE 9]>\n");
        sb.append("\t\t\t<script src=\"https://oss.maxcdn.com/html5shiv/3.7.3/html5shiv.min.js\"></script>\n");
        sb.append("\t\t\t<script src=\"https://oss.maxcdn.com/respond/1.4.2/respond.min.js\"></script>\n");
        sb.append("\t\t<![endif]-->\n");
        sb.append("\t\t<script>\n");
        if(StringUtils.isNotBlank(js)) {
            sb.append(js);
            sb.append("\n");
        }
        sb.append("\t\t</script>\n");
        sb.append("\t\t<style>\n");
        sb.append("\t\t\t.loader {\n");
        sb.append("\t\t\t\tborder: 16px solid #f3f3f3;\n");
        sb.append("\t\t\t\tborder-radius: 50%;\n");
        sb.append("\t\t\t\tborder-top: 16px solid #3498db;\n");
        sb.append("\t\t\t\twidth: 120px;\n");
        sb.append("\t\t\t\theight: 120px;\n");
        sb.append("\t\t\t\t-webkit-animation: spin 2s linear infinite;\n");
        sb.append("\t\t\t\tanimation: spin 2s linear infinite;\n");
        sb.append("\t\t\t}\n");
        sb.append("\t\t\t@-webkit-keyframes spin {\n");
        sb.append("\t\t\t\t0% { -webkit-transform: rotate(0deg); }\n");
        sb.append("\t\t\t\t100% { -webkit-transform: rotate(360deg); }\n");
        sb.append("\t\t\t}\n");
        sb.append("\t\t\t@keyframes spin {\n");
        sb.append("\t\t\t\t0% { transform: rotate(0deg); }\n");
        sb.append("\t\t\t\t100% { transform: rotate(360deg); }\n");
        sb.append("\t\t\t}\n");
        sb.append("\t\t</style>\n");
        sb.append("\t</head>\n");
        sb.append("\t<body>\n");
        sb.append("\t<div class=\"container\">\n");
        // tabs
        sb.append("\t\t<ul class=\"nav nav-pills\">\n");
        sb.append("\t\t\t<li role=\"presentation\"").append(Menu.HOME.equals(menu) ? "class=\"active\"" : "").append("><a href=\"").append(wsUrl).append("/\">Home</a></li>\n");
        sb.append("\t\t\t<li role=\"presentation\"").append(Menu.WEATHER.equals(menu) ? "class=\"active\"" : "").append("><a href=\"").append(wsUrl).append("/weather\">Weather</a></li>\n");
        sb.append("\t\t\t<li role=\"presentation\"").append(Menu.DATA_STORE.equals(menu) ? "class=\"active\"" : "").append("><a href=\"").append(wsUrl).append("/datastore\">Data Store</a></li>\n");
        sb.append("\t\t</ul><br />\n");
        sb.append("\t\t\n");
    }

    public static void addFooter(StringBuilder sb){
        sb.append("\n");
        sb.append("\t\t</div>\n");
        sb.append("\t\t<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/1.12.4/jquery.min.js\"></script>\n");
        sb.append("\t\t<script src=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js\" integrity=\"sha384-Tc5IQib027qvyjSMfHjOMaLkfuWVxZxUPnCJA7l2mCWNIpG9mGCD8wGNIcPD7Txa\" crossorigin=\"anonymous\"></script>\n");
        sb.append("\t</body>\n");
        sb.append("</html>\n");
    }

    public static void addPanel(StringBuilder sb, String title, Map<String, Object> properties){
        final StringBuilder sbPanel = new StringBuilder();
        for (Map.Entry<String, Object> property : properties.entrySet()) {
            sbPanel.append(String.format("<h4>%s <span class=\"label label-default\">%s</span></h4>", property.getKey(), property.getValue()));
        }
        addPanel(sb, title, sbPanel);
    }

    public static void addPanel(StringBuilder sb, String title, StringBuilder sbPanel){
        sb.append("<div class=\"panel panel-default\">");
        sb.append(String.format("<div class=\"panel-heading\"><h2 class=\"panel-title\">%s</h2></div>", title));
        sb.append("<div class=\"panel-body\">");
        sb.append(sbPanel);
        sb.append("</div>");
        sb.append(String.format("<div class=\"panel-footer\"><h5>Date  <b>%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS.%1$tL</b></h5></div>", new Date()));
        sb.append("</div>");
        sb.append("<br />");
    }

    public static void addAlert(StringBuilder sbPanel, String type, String message){
        // alert types: success, info, warning, danger
        sbPanel.append(String.format("<div class=\"alert alert-%s\" role=\"alert\">%s</div>", type, message));
        sbPanel.append("<br />");
    }

    public static void addEventForm(String id, String wsUrl, String label, String jsFunction, StringBuilder sbPanel){
        sbPanel.append(String.format("<form%s action=\"%s\" method=\"post\"%s>", StringUtils.isNotBlank(id) ? " id=\""+id+"\"" : "", wsUrl, StringUtils.isNotBlank(jsFunction) ? " onsubmit=\""+jsFunction+"()\"" : ""));
        sbPanel.append(String.format("<button type=\"submit\" class=\"btn btn-primary btn-lg\">%s</button>", label));
        sbPanel.append("</form>");
        sbPanel.append("<br />");
    }

    public static void addLoadingSpinner(String id, boolean hidden, StringBuilder sbPanel){
        sbPanel.append(String.format("<div class=\"loader%s\"%s></div>", hidden ? " hidden": "", StringUtils.isNotBlank(id) ? " id=\""+id+"\"" : ""));
        sbPanel.append("<br />");
    }

    public static void addWeatherForm(String wsUrl, StringBuilder sbPanel, String city){
        sbPanel.append(String.format("<form class=\"form-horizontal\" action=\"%s/weather\" method=\"get\">", wsUrl));
        sbPanel.append("<div class=\"form-group\">");
        sbPanel.append("<label for=\"city\" class=\"col-sm-2 control-label\">City</label>");
        sbPanel.append("<div class=\"col-sm-10\"><input type=\"text\" class=\"form-control\" id=\"city\" name=\"city\" placeholder=\"City\" value=\"").append(city).append("\"></div>");
        sbPanel.append("</div>");
        sbPanel.append("<div class=\"form-group\"><div class=\"col-sm-offset-2 col-sm-10\"><button type=\"submit\" class=\"btn btn-default\">Submit</button></div></div>");
        sbPanel.append("</form>");
    }

    public static void addRecordsTable(String wsUrl, StringBuilder sb, String title, List<Json> records, String extra){
        sb.append("<div class=\"panel panel-default\">");
        sb.append(String.format("<div class=\"panel-heading\"><h2 class=\"panel-title\">%s</h2></div>", title));
        sb.append("<table class=\"table\">");
        sb.append("<thead><tr><th>ID</th><th>First Name</th><th>Last Name</th></tr></thead>");
        sb.append("<tbody>");
        if(records != null && records.size() > 0) {
            for (Json record : records) {
                sb.append(String.format("<tr><th scope=\"row\"><a href=\"%s/datastore/%s\">%s</a></th><td><a href=\"%s/datastore/find/%s\">%s</a></td><td>%s</td></tr>", wsUrl, record.string("_id"), record.string("_id"), wsUrl, record.string("firstName"), record.string("firstName"),  record.string("lastName")));
            }
        } else {
            sb.append("<tr><td colspan=3>There is not records yet</td></tr>");
        }
        sb.append("</tbody>");
        sb.append("</table>");
        sb.append("<div class=\"panel-footer\">");
        sb.append(String.format("<h5>Date  <b>%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS.%1$tL</b></h5>", new Date()));
        if(StringUtils.isNotBlank(extra)) {
            sb.append(extra);
        }
        sb.append("</div>");
        sb.append("</div>");
        sb.append("<br />");
    }

    public static void addNewRecordForm(String wsUrl, StringBuilder sbPanel){
        sbPanel.append(String.format("<form class=\"form-horizontal\" action=\"%s/datastore\" method=\"post\">", wsUrl));
        sbPanel.append("<div class=\"form-group\">");
        sbPanel.append("<label for=\"lastName\" class=\"col-sm-2 control-label\">Last Name</label>");
        sbPanel.append("<div class=\"col-sm-10\"><input type=\"text\" class=\"form-control\" id=\"lastName\" name=\"lastName\" placeholder=\"Last Name\"></div>");
        sbPanel.append("</div>");
        sbPanel.append("<div class=\"form-group\">");
        sbPanel.append("<label for=\"firstName\" class=\"col-sm-2 control-label\">First Name</label>");
        sbPanel.append("<div class=\"col-sm-10\"><input type=\"text\" class=\"form-control\" id=\"firstName\" name=\"firstName\" placeholder=\"First Name\"></div>");
        sbPanel.append("</div>");
        sbPanel.append("<div class=\"form-group\"><div class=\"col-sm-offset-2 col-sm-10\"><button type=\"submit\" class=\"btn btn-default\">Save</button></div></div>");
        sbPanel.append("</form>");
    }

    public static void addUpdateRecordForm(String wsUrl, StringBuilder sbPanel, Json record){
        sbPanel.append(String.format("<form class=\"form-horizontal\" action=\"%s/datastore/%s\" method=\"post\">", wsUrl, record.string("_id")));
        sbPanel.append("<div class=\"form-group\">");
        sbPanel.append("<label for=\"lastName\" class=\"col-sm-2 control-label\">Last Name</label>");
        sbPanel.append(String.format("<div class=\"col-sm-10\"><input type=\"text\" class=\"form-control\" id=\"lastName\" name=\"lastName\" placeholder=\"Last Name\" value=\"%s\"></div>", record.string("lastName")));
        sbPanel.append("</div>");
        sbPanel.append("<div class=\"form-group\">");
        sbPanel.append("<label for=\"firstName\" class=\"col-sm-2 control-label\">First Name</label>");
        sbPanel.append(String.format("<div class=\"col-sm-10\"><input type=\"text\" class=\"form-control\" id=\"firstName\" name=\"firstName\" placeholder=\"First Name\" value=\"%s\"></div>", record.string("firstName")));
        sbPanel.append("</div>");
        sbPanel.append("<div class=\"form-group\"><div class=\"col-sm-offset-2 col-sm-10\"><button type=\"submit\" class=\"btn btn-default\">Update</button></div></div>");
        sbPanel.append("</form>");
    }

    public static void addRemoveRecordButton(String wsUrl, StringBuilder sbPanel, String recordId){
        sbPanel.append(String.format("<form action=\"%s/datastore/%s/delete\" method=\"post\">", wsUrl, recordId));
        sbPanel.append("<button type=\"submit\" class=\"btn btn-danger btn-lg\">Delete</button>");
        sbPanel.append("</form>");
        sbPanel.append("<br />");
    }

    public static void addRemoveAllRecordsButton(String wsUrl, StringBuilder sbPanel){
        sbPanel.append(String.format("<form action=\"%s/datastore/delete\" method=\"post\">", wsUrl));
        sbPanel.append("<button type=\"submit\" class=\"btn btn-danger btn-lg\">Delete all records</button>");
        sbPanel.append("</form>");
        sbPanel.append("<br />");
    }

    public static void addCountAllRecordsButton(String wsUrl, StringBuilder sbPanel){
        sbPanel.append(String.format("<form action=\"%s/datastore/count\" method=\"get\">", wsUrl));
        sbPanel.append("<button type=\"submit\" class=\"btn btn-info btn-lg\">Count all records</button>");
        sbPanel.append("</form>");
        sbPanel.append("<br />");
    }

    public static void addFindAllRecordButton(String wsUrl, StringBuilder sbPanel){
        sbPanel.append(String.format("<form action=\"%s/datastore/\" method=\"get\">", wsUrl));
        sbPanel.append("<button type=\"submit\" class=\"btn btn-info btn-lg\">Find All</button>");
        sbPanel.append("</form>");
        sbPanel.append("<br />");
    }

    public static void addFindRecordsButton(String wsUrl, StringBuilder sbPanel, String firstName){
        sbPanel.append(String.format("<form action=\"%s/datastore/find/%s\" method=\"get\">", wsUrl, firstName));
        sbPanel.append("<button type=\"submit\" class=\"btn btn-warning btn-lg\">Find records</button>");
        sbPanel.append("</form>");
        sbPanel.append("<br />");
    }

    public static void addFindRecordButton(String wsUrl, StringBuilder sbPanel, String firstName){
        sbPanel.append(String.format("<form action=\"%s/datastore/findone/%s\" method=\"get\">", wsUrl, firstName));
        sbPanel.append("<button type=\"submit\" class=\"btn btn-warning btn-lg\">Find one record</button>");
        sbPanel.append("</form>");
        sbPanel.append("<br />");
    }

    public static InputStream getFavicon() throws Exception {
        return FilesUtils.getInternalFile("webapp/images/favicon.ico");
    }
}
