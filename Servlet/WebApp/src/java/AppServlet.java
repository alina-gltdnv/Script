/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.URL;
import java.util.Objects;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.apache.commons.io.IOUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author User
 */
@WebServlet(urlPatterns = {"/AppServlet"})
public class AppServlet extends HttpServlet {

    final Pattern patternId = Pattern.compile("<requestID>(.+?)</requestID>", Pattern.DOTALL);
    final Pattern patternName = Pattern.compile("<payerName>(.+?)</payerName>", Pattern.DOTALL);
    final Pattern patternEquals = Pattern.compile("<isAddressEquals>(.+?)</isAddressEquals>", Pattern.DOTALL);
    final Pattern patternAddressReg = Pattern.compile("<regAddress>(.+?)</regAddress>", Pattern.DOTALL);
    final Pattern patternAddressRes = Pattern.compile("<resAddress>(.+?)</resAddress>", Pattern.DOTALL);
    final Pattern patterntransAmount = Pattern.compile("<transAmount>(.+?)</transAmount>", Pattern.DOTALL);
    
    Logger logger_fail = LogManager.getLogger("webapp.user_fail");
    Logger logger_pass = LogManager.getLogger("webapp.user_pass");

    public DataBaseUser dbServlet = new DataBaseUser();
    public Schema schema;

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void init() {

        try {
            dbServlet.readDataBase("C:\\Share\\datapool_web.dat");
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            schema = schemaFactory.newSchema(new File("C:\\Share\\rq.xsd"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try ( PrintWriter out = response.getWriter()) {
            //получили в строку наше входное сообщение
            String req = IOUtils.toString(request.getInputStream());

            //создаем стринги в которых будет результат 
            String req_id1 = "";
            String resultValidate = "";
            String resultCheck = "";
            xsdMessage msgXsdValid = new xsdMessage();

            //создаем юзера которого будем искать если запрос валидный
            User inputUser;

            //валидируем сообщение и проверяем транзакцию
            if (validateXML(req, msgXsdValid)) {
                //мы тут - если сообщение валидное
                //получили значение тега requestID из входного собщения
                Matcher matcher_id = patternId.matcher(req);
                matcher_id.find();
                req_id1 = matcher_id.group(1);

                //получили значение тега payerName из входного собщения
                Matcher matcher_patternName = patternName.matcher(req);
                matcher_patternName.find();
                String req_name = matcher_patternName.group(1);

                //получили значение тега isAddressEquals из входного собщения
                Matcher matcher_patternEquals = patternEquals.matcher(req);
                matcher_patternEquals.find();
                boolean req_bool = Boolean.parseBoolean(matcher_patternEquals.group(1));

                //получили значение тега regAddress из входного собщения
                Matcher matcher_patternAddressReg = patternAddressReg.matcher(req);
                matcher_patternAddressReg.find();
                String req_adress_reg = matcher_patternAddressReg.group(1);
                
                //получили значение тега regAddress из входного собщения
                Matcher matcher_patterntransAmount = patterntransAmount.matcher(req);
                matcher_patterntransAmount.find();
                String ammount = matcher_patterntransAmount.group(1);

                //получаем значение  resAddress из входного собщения или приравниваем к regAddress
                String req_adress_res = "";
                if (req_bool) {
                    req_adress_res = req_adress_reg;
                } else {
                    Matcher matcher_patternAddressRes = patternAddressRes.matcher(req);
                    matcher_patternAddressRes.find();
                    req_adress_res = matcher_patternAddressRes.group(1);
                }
                //создаем юзера
                inputUser = new User(req_name, req_adress_reg, req_adress_res);
                //сохраняем результат валидации
                resultValidate = msgXsdValid.msg;
                //ищем юзера в бд
                resultCheck = dbServlet.checkUser(inputUser);
                //Если юзер не найден - пишем это в описании
                if (resultCheck.equals("FAIL")) {
                    resultValidate = "NOT FOUND USER";
                    logger_fail.info("id="+req_id1 + ", name="+req_name + ", ammount="+ammount);
                }
                else
                {
                    logger_pass.info("id="+req_id1 + ", name="+req_name + ", ammount="+ammount);
                }
            } else {
                //мы тут - если сообщение не валидное
                resultValidate = msgXsdValid.msg;
                resultCheck = "ERROR";
            }

            //заполняем ответ и отвечаем
            String responsestring = 
                    "<CheckRs>\n"
                        + "<requestID>" + req_id1 + "</requestID>\n"
                        + "<result>" + resultCheck + "</result>\n"
                        + "<description>" + resultValidate + "</description>\n"
                    + "</CheckRs>";

            out.println(responsestring);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // метод валидации
    private boolean validateXML(String request, xsdMessage ms) {
        Validator validator;
        try {
            validator = schema.newValidator();
            validator.validate(new StreamSource(new StringReader(request)));
            ms.msg = "OK";
            return true;
        } catch (Exception e) {
            ms.msg = e.getMessage();
            return false;
        }
    }

    //не используем
    private String readFile(String path) {
        String outFile = "";
        try {
            FileReader reader = new FileReader(path);
            Scanner scan = new Scanner(reader);
            while (scan.hasNextLine()) {
                outFile += scan.nextLine();
            }
            reader.close();
            return outFile;
        } catch (Exception ex) {
            return "";
        }
    }

    //не используем
    private String getResource(String filename) throws FileNotFoundException {
        URL resource = getClass().getClassLoader().getResource(filename);
        Objects.requireNonNull(resource);
        return resource.getFile();
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

//вспомогательный класс
class xsdMessage {
    public String msg;
}
