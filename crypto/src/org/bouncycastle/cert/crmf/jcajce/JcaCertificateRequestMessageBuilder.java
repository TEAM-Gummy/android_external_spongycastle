package org.bouncycastle.cert.crmf.jcajce;

import java.math.BigInteger;
import java.security.PublicKey;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.cert.crmf.CertificateRequestMessageBuilder;

public class JcaCertificateRequestMessageBuilder
    extends CertificateRequestMessageBuilder
{
    public JcaCertificateRequestMessageBuilder(BigInteger certReqId)
    {
        super(certReqId);
    }

    public JcaCertificateRequestMessageBuilder setIssuer(X500Principal issuer)
    {
        if (issuer != null)
        {
            setIssuer(X509Name.getInstance(issuer.getEncoded()));
        }

        return this;
    }

    public JcaCertificateRequestMessageBuilder setSubject(X500Principal subject)
    {
        if (subject != null)
        {
            setSubject(X509Name.getInstance(subject.getEncoded()));
        }

        return this;
    }

    public JcaCertificateRequestMessageBuilder setAuthInfoSender(X500Principal sender)
    {
        if (sender != null)
        {
            setAuthInfoSender(new GeneralName(X509Name.getInstance(sender.getEncoded())));
        }

        return this;
    }

    public JcaCertificateRequestMessageBuilder setPublicKey(PublicKey publicKey)
    {
        setPublicKey(SubjectPublicKeyInfo.getInstance(publicKey.getEncoded()));

        return this;
    }
}
