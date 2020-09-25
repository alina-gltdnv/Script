/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LRLib;

import lrapi.lr;
import lrapi.web;
import java.util.UUID;

/**
 *
 * @author User
 */
public class soapAction2 extends LRAction {

    public int init() {

        return 0;
    }

    public int action() {
        try {
            lr.continue_on_error(0);
            String equal;
            String factaddress;
            String htmlBody;
            String id1 = String.valueOf(UUID.randomUUID());

            if (lr.eval_string("{addressreg}").equals(lr.eval_string("{addressfact}"))) {
                factaddress = "";
                equal = "True";
            } else {
                factaddress = lr.eval_string("{addressfact}");
                equal = "False";
            }
            htmlBody =  "Body=<CheckRq>"
                    + "<requestID>" + id1 + "</requestID>"
                    + "<payerName>" + lr.eval_string("{firstname}") + " " + lr.eval_string("{middlename}") + " " + lr.eval_string("{lastname}") + "</payerName>"
                    + "<isAddressEquals>" + equal + "</isAddressEquals>"
                    + "<regAddress>" + lr.eval_string("{addressreg}") + "</regAddress>"
                    + "<resAddress>" + factaddress + "</resAddress>"
                    + "<transAmount>" + lr.eval_string("{randamount}") + "</transAmount>"
                    + "<currency>RUB</currency>"
                    + "</CheckRq>";
            web.reg_save_param("Final",
                    new String[]{
                        // "NOTFOUND=ERROR", 
                        "LB=<result>",
                        "RB=</result>",
                        "LAST"});
            lr.start_transaction("Transfer");
            web.custom_request("post_query.exe",
                    "Method=POST",
                    new String[]{
                        "URL=" + lr.eval_string("{url}"),
                        htmlBody,
                        "LAST"});
            if (lr.eval_string("{Final}").equals("PASS")) {
                lr.end_transaction("Transfer", lr.PASS);
            } else {
                lr.end_transaction("Transfer", lr.FAIL);
            }

            System.out.println(lr.eval_string("{Final}"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    public int end() {
        return 0;
    }
}
