
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author User
 */
public class DataBaseUser {

    public Map<Integer, User> DB;

    public void readDataBase(String path) {
        DB = new HashMap();
        try {
            FileReader reader = new FileReader(path);
            Scanner scan = new Scanner(reader);
            int i = 0;
            while (scan.hasNextLine()) {
                String[] massive = scan.nextLine().split(";");
                DB.put(i, new User(massive[1] + " " + massive[2] + " " + massive[3], massive[4], massive[5]));
                i++;
            }
            System.out.println("loading successfull,user count="+i);
            
            reader.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public String checkUser(User inputUser) {
        for (int i = 0; i < DB.size(); i++) {
            if (compareUsers(inputUser, DB.get(i))) {
                return "PASS";
            }
        }
        return "FAIL";
    }

    public boolean compareUsers(User inputUser, User inputUser2) {
        if (inputUser.payerName.equals(inputUser2.payerName)
                & inputUser.regAddress.equals(inputUser2.regAddress)
                & inputUser.resAddress.equals(inputUser2.resAddress)) {
            return true;
        } else {
            return false;
        }

    }
}
