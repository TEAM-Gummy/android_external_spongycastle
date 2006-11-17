package org.bouncycastle.openpgp.examples;

import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPCompressedData;
import org.bouncycastle.openpgp.PGPCompressedDataGenerator;
import org.bouncycastle.openpgp.PGPEncryptedData;
import org.bouncycastle.openpgp.PGPEncryptedDataGenerator;
import org.bouncycastle.openpgp.PGPEncryptedDataList;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPLiteralData;
import org.bouncycastle.openpgp.PGPObjectFactory;
import org.bouncycastle.openpgp.PGPPBEEncryptedData;
import org.bouncycastle.openpgp.PGPUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;

/**
 * A simple utility class that encrypts/decrypts password based
 * encryption files.
 * <p>
 * To encrypt a file: PBEFileProcessor -e [-ai] fileName passPhrase.<br>
 * If -a is specified the output file will be "ascii-armored".<br>
 * If -i is specified the output file will be "integrity protected".
 * <p>
 * To decrypt: PBEFileProcessor -d fileName passPhrase.
 * <p>
 * Note: this example will silently overwrite files, nor does it pay any attention to
 * the specification of "_CONSOLE" in the filename. It also expects that a single pass phrase
 * will have been used.
 */
public class PBEFileProcessor
{
    /**
     * decrypt the passed in message stream
     */
    private static void decryptFile(
        InputStream    in,
        char[]         passPhrase)
        throws Exception
    {
        in = PGPUtil.getDecoderStream(in);
        
        PGPObjectFactory        pgpF = new PGPObjectFactory(in);
        PGPEncryptedDataList    enc;
        Object                  o = pgpF.nextObject();
        
        //
        // the first object might be a PGP marker packet.
        //
        if (o instanceof PGPEncryptedDataList)
        {
            enc = (PGPEncryptedDataList)o;
        }
        else
        {
            enc = (PGPEncryptedDataList)pgpF.nextObject();
        }

        PGPPBEEncryptedData     pbe = (PGPPBEEncryptedData)enc.get(0);

        InputStream clear = pbe.getDataStream(passPhrase, "BC");
        
        PGPObjectFactory        pgpFact = new PGPObjectFactory(clear);
        
        PGPCompressedData       cData = (PGPCompressedData)pgpFact.nextObject();
    
        pgpFact = new PGPObjectFactory(cData.getDataStream());
        
        PGPLiteralData          ld = (PGPLiteralData)pgpFact.nextObject();
        
        FileOutputStream        fOut = new FileOutputStream(ld.getFileName());
        
        InputStream    unc = ld.getInputStream();
        int    ch;
        
        while ((ch = unc.read()) >= 0)
        {
            fOut.write(ch);
        }
        
        if (pbe.isIntegrityProtected())
        {
            if (!pbe.verify())
            {
                System.err.println("message failed integrity check");
            }
            else
            {
                System.err.println("message integrity check passed");
            }
        }
        else
        {
            System.err.println("no message integrity check");
        }
    }

    private static void encryptFile(
        OutputStream    out,
        String          fileName,
        char[]          passPhrase,
        boolean         armor,
        boolean         withIntegrityCheck)
        throws IOException, NoSuchProviderException, PGPException
    {    
        if (armor)
        {
            out = new ArmoredOutputStream(out);
        }
        
        ByteArrayOutputStream       bOut = new ByteArrayOutputStream();
        

        PGPCompressedDataGenerator  comData = new PGPCompressedDataGenerator(
                                                                PGPCompressedData.ZIP);
                                                                
        PGPUtil.writeFileToLiteralData(comData.open(bOut), PGPLiteralData.BINARY, new File(fileName));
        
        comData.close();
        
        PGPEncryptedDataGenerator   cPk = new PGPEncryptedDataGenerator(PGPEncryptedData.CAST5, withIntegrityCheck, new SecureRandom(), "BC");
            
        cPk.addMethod(passPhrase);
        
        byte[]                      bytes = bOut.toByteArray();
        
        OutputStream                cOut = cPk.open(out, bytes.length);

        cOut.write(bytes);
        
        cOut.close();
        
        out.close();
    }

    public static void main(
        String[] args)
        throws Exception
    {
        Security.addProvider(new BouncyCastleProvider());

        if (args[0].equals("-e"))
        {
            if (args[1].equals("-a") || args[1].equals("-ai") || args[1].equals("-ia"))
            {
                FileOutputStream    out = new FileOutputStream(args[2] + ".asc");
                encryptFile(out, args[2], args[3].toCharArray(), true, (args[1].indexOf('i') > 0));
            }
            else if (args[1].equals("-i"))
            {
                FileOutputStream    out = new FileOutputStream(args[2] + ".bpg");
                encryptFile(out, args[2], args[3].toCharArray(), false, true);
            }
            else
            {
                FileOutputStream    out = new FileOutputStream(args[1] + ".bpg");
                encryptFile(out, args[1], args[2].toCharArray(), false, false);
            }
        }
        else if (args[0].equals("-d"))
        {
            FileInputStream    in = new FileInputStream(args[1]);
            decryptFile(in, args[2].toCharArray());
        }
        else
        {
            System.err.println("usage: PBEFileProcessor -e [-ai]|-d file passPhrase");
        }
    }
}
