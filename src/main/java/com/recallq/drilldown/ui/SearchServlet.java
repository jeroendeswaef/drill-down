package com.recallq.drilldown.ui;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.recallq.drilldown.ui.service.SearchResultsService;
import com.recallq.drilldown.ui.service.TimeRangeType;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Controller for the ajax action that returns JSON after hitting the "Search"
 * button.
 *
 * @author Jeroen De Swaef <j@recallq.com>
 */
public class SearchServlet extends CommonDrillDownServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/json;charset=UTF-8");
        String requestLike = new String();
        if (request.getParameterMap().containsKey("requestLike")) {
            requestLike = request.getParameter("requestLike");
        }

        TimeRangeType selectedTimeRange;
        if (request.getParameterMap().containsKey("timeRange")) {
            selectedTimeRange = TimeRangeType.valueOf(request.getParameter("timeRange"));
        } else {
            selectedTimeRange = TimeRangeType.LAST_WEEK;
        }

        SearchResultsService service = super.getSearchService();
        // default values
        Map<String, Object> results = service.getResults(requestLike, selectedTimeRange);

        JsonObject returnObj = new JsonObject();

        for (Map.Entry<String, Object> resultEntry : results.entrySet()) {

            if (resultEntry.getValue() instanceof JsonElement) {

                returnObj.add(resultEntry.getKey(), (JsonElement) resultEntry.getValue());

            } else if (resultEntry.getValue() instanceof Long) {
                returnObj.addProperty(resultEntry.getKey(), (Long) resultEntry.getValue());
            }
        }
        Gson gson = new Gson();
        String jsonStr = gson.toJson(returnObj);
        PrintWriter out = response.getWriter();
        try {

            out.println(jsonStr);

        } finally {
            out.close();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
