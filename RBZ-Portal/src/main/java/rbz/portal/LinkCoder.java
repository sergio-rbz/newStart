/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rbz.portal;

import java.io.UnsupportedEncodingException;
import java.util.Base64;

/**
 * @author EDV-PC-Andreas
 */
public class LinkCoder {

    public String encode(String link) throws UnsupportedEncodingException {
        String encodedLink = Base64.getEncoder().encodeToString(link.getBytes("utf-8"));
        byte[] decrypted = Base64.getDecoder().decode(encodedLink.getBytes("utf-8"));
        String s = new String(decrypted);
        return encodedLink;
    }

    public String decode(String encodedlink) throws UnsupportedEncodingException {
        byte[] decrypted = Base64.getDecoder().decode(encodedlink.getBytes("utf-8"));
        String s = new String(decrypted);
        return s;
    }
}
