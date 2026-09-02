/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rbz.portal;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import java.io.Serializable;
import java.util.UUID;

/**
 * @author andreas
 */
@ManagedBean(name = "SchuleAdminHelper")
@SessionScoped
public class Helper implements Serializable {

    public String Classname_Check(String className) {
        String s = className;
        String classNameLastLetter = className.substring(className.length() - 1);
        if (!s.matches("^[a-zA-Z]*$")) {
            classNameLastLetter = classNameLastLetter.toLowerCase();
            if (classNameLastLetter.matches("^[a-z]+$")) {
                className = className.trim().replace(" ", "");
                className = className.toUpperCase();
                className = className.substring(0, className.length() - 1);
                classNameLastLetter = classNameLastLetter.toLowerCase();
                className = className + classNameLastLetter;
            }
        }
        return className;
    }

    public class Int2String {

        public void main(String[] args) {
            int i = 5;
            Integer meinInteger = new Integer(i);
            String s = meinInteger.toString();
            System.out.println(s);
        }
    }

    public String GenerateKey() {
        String uuid = UUID.randomUUID().toString();
        //System.out.println("uuid = " + uuid);
        String[] arr1 = uuid.split("-");
        return arr1[4].substring(0, 7);
    }

    public String Replace_Umlauts(String str) {
        str = str.replaceAll("Ä", "AE");
        str = str.replaceAll("ä", "ae");
        str = str.replaceAll("Ü", "UE");
        str = str.replaceAll("ü", "ue");
        str = str.replaceAll("Ö", "OE");
        str = str.replaceAll("ö", "oe");
        str = str.replaceAll("ß", "ss");
        return str;
    }

    public String setKapital(String strg) {
        String arr[] = strg.split(" ");
        strg = "";
        for (String str : arr) {
            String f = str.substring(0, 1).toUpperCase();
            String f2 = str.substring(1);
            strg = strg + f + f2;
            strg = strg.trim();
            strg = strg + " ";
        }
        return strg.trim();
    }
}
