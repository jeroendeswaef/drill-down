
package com.recallq.drilldown.ui;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.recallq.drilldown.ui.service.SearchResultsService;
import com.recallq.drilldown.ui.service.TimeRangeType;
import java.io.IOException;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Controller for the initial page.
 * 
 * @author Jeroen De Swaef <j@recallq.com>
 */
public class DashboardServlet extends CommonDrillDownServlet {

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
    
            SearchResultsService service = super.getSearchService();
            
            // default values
            Map<String, Object> results = service.getResults(new String(), TimeRangeType.LAST_WEEK);
               
            for (Map.Entry<String, Object> resultEntry: results.entrySet()) {
                if (resultEntry instanceof JsonElement) {
                    Gson gson = new Gson();
                    String jsonStr = gson.toJson((JsonElement) resultEntry);
                    request.setAttribute(resultEntry.getKey(), jsonStr);
                } else {
                    request.setAttribute(resultEntry.getKey(), resultEntry.getValue());
                }
            }
           
            request.getRequestDispatcher("index.jsp").forward(request, response);

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
