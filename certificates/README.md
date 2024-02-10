<h3 align=center> Certificates </h3>

The JRE Vanilla ships with is very outdated on older versions so it does not have the ISRG Root X1 or ISRG Root X2 on it's keystore, our backend uses Let's Encrypt which uses these root certificates so we must ship a custom keystore for our HTTP Client to use, otherwise it will not be able to authenticate.
These were downloaded directly from [Let's Encrypt's page](https://letsencrypt.org/certificates/) and Java's builtin keytool was used to create the keystore, you can find it [here](/fabric/src/main/resources) and [here](/forge/src/main/resources).