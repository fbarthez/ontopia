/*
 * #!
 * Ontopia Engine
 * #-
 * Copyright (C) 2001 - 2013 The Ontopia Project
 * #-
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * !#
 */

package net.ontopia.infoset.impl.basic;

import java.net.URISyntaxException;
import junit.framework.TestCase;
import net.ontopia.infoset.core.LocatorIF;
import net.ontopia.utils.OntopiaRuntimeException;

public class URITest extends TestCase {

  public URITest(String name) {
    super(name);
  }
  
  // --- normalization

  public void testHttpOrdinary() {
    normalizesTo("http://www.ontopia.net/", "http://www.ontopia.net/");
  }

  public void testHttpPort80() {
    normalizesTo("http://www.ontopia.net:80", "http://www.ontopia.net:80");
  }

  public void testHttpCaseSensitive() {
    normalizesTo("http://www.ONTOPIA.net/temp.html#README",
		 "http://www.ontopia.net/temp.html#README");
  }

  public void testHttpPort80Slash() {
    normalizesTo("http://www.ontopia.net:80/", "http://www.ontopia.net:80/");
  }

  public void testHttpPort8080Slash() {
    normalizesTo("http://www.ontopia.net:8080/",
		 "http://www.ontopia.net:8080/");
  }

  public void testFtpOrdinary() {
    normalizesTo("ftp://ftp.ontopia.net/pub",
		 "ftp://ftp.ontopia.net/pub");
  }

  public void testFtpOrdinarySlash() {
    normalizesTo("ftp://ftp.ontopia.net/pub/",
		 "ftp://ftp.ontopia.net/pub/");
  }

  public void testFileOrdinary() {
    normalizesTo("file:///ifikurs.xtm#in105",
		 "file:/ifikurs.xtm#in105");
  }

  public void testFileJavaStyle() {
    normalizesTo("file:/home/larsga/cvs-co/src/java/tst.py",
		 "file:/home/larsga/cvs-co/src/java/tst.py");
  }

  public void testFileOperaStyle() {
    normalizesTo("file://localhost/home/larsga/.bashrc",
		 "file://localhost/home/larsga/.bashrc");
  }
  
  public void testFileOperaStyleUpcase() {
    normalizesTo("file://LOCALHOST/home/larsga/.bashrc",
		 "file://LOCALHOST/home/larsga/.bashrc");
  }
  
  public void testUNCFileNames() {
    normalizesTo("file://server/directory/file.doc",
                 "file://server/directory/file.doc");
  }

  public void testDoubleSlash() {
    normalizesTo("http://www.ontopia.net/a//b/c.html",
		 "http://www.ontopia.net/a/b/c.html");
  }

  public void testUpOneDir() {
    normalizesTo("http://www.ontopia.net/a/../b/c.html",
		 "http://www.ontopia.net/b/c.html");
  }

  public void testUpTwoDirs() {
    normalizesTo("http://www.ontopia.net/a/d/../../b/c.html",
		 "http://www.ontopia.net/b/c.html");
  }

  public void testUpThreeDirs() {
    normalizesTo("http://www.ontopia.net/a/d/e/../../../b/c.html",
		 "http://www.ontopia.net/b/c.html");
  }

  public void testUpOneDirTooFar() {
    normalizesTo("http://www.ontopia.net/a/d/e/../../../../b/c.html",
		 "http://www.ontopia.net/../b/c.html");
  }

  public void testSingleDotDir() {
    normalizesTo("http://www.ontopia.net/a/./b/c.html",
		 "http://www.ontopia.net/a/b/c.html");
  }

  public void testUppercaseUserAndPassword() {
    normalizesTo("http://JUSTIN:PASSWORD@WWW.VLC.COM.AU/ABC",
		 "http://JUSTIN:PASSWORD@www.vlc.com.au/ABC");
  }

  public void testVLC1() {
    normalizesTo("http://www.vlc.com.au/something",
		 "http://www.vlc.com.au/something");
  }

  public void testVLC10() {
    normalizesTo("http://www.vlc.com.au/",
		 "http://www.vlc.com.au/");
  }

  public void testVLC11() {
    normalizesTo("http://www.vlc.com.au:8080/",
		 "http://www.vlc.com.au:8080/");
  }

  public void testVLC13() {
    normalizesTo("http://justin@www.vlc.com.au:8080/",
		 "http://justin@www.vlc.com.au:8080/");
  }

  public void testVLC15() {
    normalizesTo("http://justin:password@www.vlc.com.au:8080/",
		 "http://justin:password@www.vlc.com.au:8080/");
  }

  public void testVLC17() {
    normalizesTo("http://justin:password@www.vlc.com.au/",
		 "http://justin:password@www.vlc.com.au/");
  }

  public void testRFC2396_1() {
    normalizesTo("ftp://ftp.is.co.za/rfc/rfc1808.txt",
		 "ftp://ftp.is.co.za/rfc/rfc1808.txt");
  }

  public void testRFC2396_3() {
    normalizesTo("http://www.math.uio.no/faq/compression-faq/part1.html",
		 "http://www.math.uio.no/faq/compression-faq/part1.html");
  }

  public void testRFC2396_4() {
    normalizesTo("mailto:mduerst@ifi.unizh.ch",
		 "mailto:mduerst@ifi.unizh.ch");
  }

  public void testRFC2396_5() {
    normalizesTo("news:comp.infosystems.www.servers.unix",
		 "news:comp.infosystems.www.servers.unix");
  }

  public void testRFC2396_6() {
    normalizesTo("telnet://melvyl.ucop.edu/",
		 "telnet://melvyl.ucop.edu/");
  }
  
  public void testSingleDotAtEnd() {
    normalizesTo("http://www.math.uio.no/.",
		 "http://www.math.uio.no/");
  }

  public void testSingleDotAtEndWithQuery() {
    normalizesTo("http://www.math.uio.no/.?query",
		 "http://www.math.uio.no/?query");
  }

  // --- check illegal URIs

  public void testEmpty() {
    verifyIllegal("");
  }
  
  public void testEmpty2() {
    verifyIllegal(":");
  }
  
  public void testNoTermination() {
    verifyIllegal("http");
  }
  
  public void testWrongTermination() {
    verifyIllegal("http/");
  }

  public void testIllegalCharacterInScheme() {
    verifyIllegal("URI|file:/tst.txt");
  }

  public void testTwoHashCharacters() {
    verifyIllegal("http://www.viessmann.com#test#again");
  }

   public void testWhitespace() throws URISyntaxException {
     verifyIllegal("  ftp://ftp.ontopia.net/pub/  ");
   }

  public void testNonAsciiCharsInFragment() throws URISyntaxException {
    normalizesTo("http://www.math.uio.no/abc/#f\u00F8\u00F8", "http://www.math.uio.no/abc/#f\u00F8\u00F8");
  }
  
  // --- relative URI resolution

  public void testAbsoluteResolution() {
    resolvesTo("http://www.ontopia.net:8080/ugga/bugga.xtm",
	       "http://www.garshol.priv.no/rock.xtm",
	       "http://www.garshol.priv.no/rock.xtm");
  }

  public void testFragmentResolution() {
    resolvesTo("http://www.ontopia.net:8080/ugga/bugga.xtm",
	       "#boogie",
	       "http://www.ontopia.net:8080/ugga/bugga.xtm#boogie");
  }

  public void testFileResolution() {
    resolvesTo("http://www.ontopia.net:8080/ugga/bugga.xtm",
	       "boggu.xtm",
	       "http://www.ontopia.net:8080/ugga/boggu.xtm");
  }

  public void testDownDirResolution() {
    resolvesTo("http://www.ontopia.net:8080/ugga/bugga.xtm",
	       "ugga/boggu.xtm",
	       "http://www.ontopia.net:8080/ugga/ugga/boggu.xtm");
  }

  public void testUpDirResolution() {
    resolvesTo("http://www.ontopia.net:8080/ugga/bugga.xtm",
	       "../boggu.xtm",
	       "http://www.ontopia.net:8080/boggu.xtm");
  }

  public void testSameFileFragmentResolution() {
    resolvesTo("http://www.ontopia.net:8080/ugga/bugga.xtm",
	       "bugga.xtm#rongo",
	       "http://www.ontopia.net:8080/ugga/bugga.xtm#rongo");
  }

  public void testFileSameFileFragmentResolution() {
    resolvesTo("file:/home/larsga/tmp/out.xtm",
	       "out.xtm#in",
	       "file:/home/larsga/tmp/out.xtm#in");
  }

  public void testFileFragmentResolution() {
    resolvesTo("file:/home/larsga/tmp/out.xtm",
	       "#in",
	       "file:/home/larsga/tmp/out.xtm#in");
  }

  public void testFileOperaFuckup() {
    LocatorIF base = URILocator.create("file:/home/larsga/cvs-co/topicmaps/opera/opera.xtm");
    LocatorIF base2 = base.resolveAbsolute("opera-template.xtm");
    LocatorIF abs = base2.resolveAbsolute("geography.xtm");

    assertTrue("Two-step normalization produced wrong result",
	   abs.getAddress().equals("file:/home/larsga/cvs-co/topicmaps/opera/geography.xtm"));
  }

  public void testFileDownDirResolution() {
    resolvesTo("file:/home/larsga/tmp/out.xtm",
	       "out/out.xtm",
	       "file:/home/larsga/tmp/out/out.xtm");
  }

  public void testFileUpDirResolution() {
    resolvesTo("file:/home/larsga/tmp/out.xtm",
	       "../out.xtm",
	       "file:/home/larsga/out.xtm");
  }

  public void testFileUpOneDirTooFarResolution() {
    resolvesTo("file:/home/out.xtm",
	       "../../out.xtm",
	       "file:/../out.xtm");
  }

  public void testFragmentWithLatin1() {
    resolvesTo("file:/home/larsga/tmp/out.xtm",
	       "#V_AM\u00F8ller",
	       "file:/home/larsga/tmp/out.xtm#V_AM\u00F8ller");
  }

  public void testFragmentWithNonLatin1() {
    resolvesTo("file:/home/larsga/tmp/out.xtm",
	       "#V_AM\u01F8ller",
	       "file:/home/larsga/tmp/out.xtm#V_AM\u01F8ller");
  }

  public void testMailTo() {
    resolvesTo("mailto:larsga@ontopia.net",
	       "http://www.ontopia.net:8080/ugga/bugga.xtm",
	       "http://www.ontopia.net:8080/ugga/bugga.xtm");
  }

  public void testRFC2396C_1() {
    resolvesTo("http://a/b/c/d;p?q",
	       "g:h",
	       "g:h");
  }
  
  public void testRFC2396C_2() {
    resolvesTo("http://a/b/c/d;p?q",
	       "g",
	       "http://a/b/c/g");
  }
  
  public void testRFC2396C_3() {
    resolvesTo("http://a/b/c/d;p?q",
	       "./g",
	       "http://a/b/c/g");
  }
  
  public void testRFC2396C_4() {
    resolvesTo("http://a/b/c/d;p?q",
	       "g/",
	       "http://a/b/c/g/");
  }
  
  public void testRFC2396C_5() {
    resolvesTo("http://a/b/c/d;p?q",
	       "/g",
	       "http://a/g");
  }

    public void testRFC2396C_5Variant() {
      resolvesTo("http://a/b/c/d;p?q",
  	       "/g/../y",
  	       "http://a/y");
    }
  
  public void testRFC2396C_6() {
    resolvesTo("http://a/b/c/d;p?q",
	       "//g",
	       "http://g");
  }
  
  public void testRFC2396C_7() {
    resolvesTo("http://a/b/c/d;p?q",
	       "?y",
	       "http://a/b/c/?y");
  }
  
  public void testRFC2396C_8() {
    resolvesTo("http://a/b/c/d;p?q",
	       "g?y",
	       "http://a/b/c/g?y");
  }
  
  public void testRFC2396C_9() {
    resolvesTo("http://a/b/c/d;p?q",
	       "#s",
	       "http://a/b/c/d;p?q#s");
  }
  
  public void testRFC2396C_10() {
    resolvesTo("http://a/b/c/d;p?q",
	       "g#s",
	       "http://a/b/c/g#s");
  }
  
  public void testRFC2396C_11() {
    resolvesTo("http://a/b/c/d;p?q",
	       "g?y#s",
	       "http://a/b/c/g?y#s");
  }
  
  public void testRFC2396C_12() {
    resolvesTo("http://a/b/c/d;p?q",
	       ";x",
	       "http://a/b/c/;x");
  }
  
  public void testRFC2396C_13() {
    resolvesTo("http://a/b/c/d;p?q",
	       "g;x",
	       "http://a/b/c/g;x");
  }
  
  public void testRFC2396C_14() {
    resolvesTo("http://a/b/c/d;p?q",
	       "g;x?y#s",
	       "http://a/b/c/g;x?y#s");
  }
  
  public void testRFC2396C_15() {
    resolvesTo("http://a/b/c/d;p?q",
	       ".",
	       "http://a/b/c/");
  }
  
  public void testRFC2396C_16() {
    resolvesTo("http://a/b/c/d;p?q",
	       "./",
	       "http://a/b/c/");
  }
  
  public void testRFC2396C_17() {
    resolvesTo("http://a/b/c/d;p?q",
	       "../",
	       "http://a/b/");
  }
  
  public void testRFC2396C_18() {
    resolvesTo("http://a/b/c/d;p?q",
	       "..",
	       "http://a/b/");
  }
  
  public void testRFC2396C_19() {
    resolvesTo("http://a/b/c/d;p?q",
	       "../g",
	       "http://a/b/g");
  }
  
  public void testRFC2396C_20() {
    resolvesTo("http://a/b/c/d;p?q",
	       "../..",
	       "http://a/");
  }
  
  public void testRFC2396C_21() {
    resolvesTo("http://a/b/c/d;p?q",
	       "../../",
	       "http://a/");
  }
  
  public void testRFC2396C_22() {
    resolvesTo("http://a/b/c/d;p?q",
	       "../../g",
	       "http://a/g");
  }
  
  public void testRFC2396C_23() {
    resolvesTo("http://a/b/c/d;p?q",
	       "",
	       "http://a/b/c/d;p?q");
  }
  
  public void testRFC2396C_24() {
    resolvesTo("http://a/b/c/d;p?q",
	       "../../../g",
	       "http://a/../g");
  }
  
  public void testRFC2396C_25() {
    resolvesTo("http://a/b/c/d;p?q",
	       "../../../../g",
	       "http://a/../../g");
  }
  
  public void testRFC2396C_26() {
    resolvesTo("http://a/b/c/d;p?q",
	       "g.",
	       "http://a/b/c/g.");
  }
  
  public void testRFC2396C_27() {
    resolvesTo("http://a/b/c/d;p?q",
	       ".g",
	       "http://a/b/c/.g");
  }
  
  public void testRFC2396C_28() {
    resolvesTo("http://a/b/c/d;p?q",
	       "g..",
	       "http://a/b/c/g..");
  }
  
  public void testRFC2396C_29() {
    resolvesTo("http://a/b/c/d;p?q",
	       "..g",
	       "http://a/b/c/..g");
  }
  
  public void testRFC2396C_30() {
    resolvesTo("http://a/b/c/d;p?q",
	       "./../g",
	       "http://a/b/g");
  }
  
  public void testRFC2396C_31() {
    resolvesTo("http://a/b/c/d;p?q",
	       "./g/.",
	       "http://a/b/c/g/");
  }
  
  public void testRFC2396C_32() {
    resolvesTo("http://a/b/c/d;p?q",
	       "g/./h",
	       "http://a/b/c/g/h");
  }
  
  public void testRFC2396C_33() {
    resolvesTo("http://a/b/c/d;p?q",
	       "g/../h",
	       "http://a/b/c/h");
  }
  
  public void testRFC2396C_34() {
    resolvesTo("http://a/b/c/d;p?q",
	       "g;x=1/./y",
	       "http://a/b/c/g;x=1/y");
  }
  
  public void testRFC2396C_35() {
    resolvesTo("http://a/b/c/d;p?q",
	       "g;x=1/../y",
	       "http://a/b/c/y");
  }
  
  public void testRFC2396C_36() {
    resolvesTo("http://a/b/c/d;p?q",
	       "g?y/./x",
	       "http://a/b/c/g?y/./x");
  }
  
  public void testRFC2396C_37() {
    resolvesTo("http://a/b/c/d;p?q",
	       "g?y/../x",
	       "http://a/b/c/g?y/../x");
  }
  
  public void testRFC2396C_38() {
    resolvesTo("http://a/b/c/d;p?q",
	       "g#s/./x",
	       "http://a/b/c/g#s/./x");
  }
  
  public void testRFC2396C_39() {
    resolvesTo("http://a/b/c/d;p?q",
	       "g#s/../x",
	       "http://a/b/c/g#s/../x");
  }

  public void testExposedByLTM() {
    resolvesTo("http://psi.ontopia.net/",
	       "ontopia/ontopia.xtm#ontopia",
	       "http://psi.ontopia.net/ontopia/ontopia.xtm#ontopia");
  }

  public void testExposedByLTM3() {
    resolvesTo("http://psi.ontopia.net/",
	       "ontopia",
	       "http://psi.ontopia.net/ontopia");
  }

  // http://www.apache.org/~fielding/uri/rev-2002/issues.html#017-rdf-fragment
  public void testRDFCaseI() {
    resolvesTo("http://example.org/dir/file#frag",
	       "#foo",
	       "http://example.org/dir/file#foo");
  }

  public void testRDFCaseJ() {
    resolvesTo("http://example.org/dir/file#frag",
	       "",
	       "http://example.org/dir/file");
  }

  // --- equals

  public void testEqual() throws URISyntaxException {
    URILocator loc1 = new URILocator("http://www.ontopia.net");    
    URILocator loc2 = new URILocator("http://www.ontopia.net");
    assertTrue("URILocator does not equal itself",
	   loc1.equals(loc2));
  }

  public void testNotEqual2() throws URISyntaxException {
    URILocator loc1 = new URILocator("http://www.ontopia.net");    
    URILocator loc2 = new URILocator("http://www.ontopia.com");
    assertTrue("URILocator equals different URI",
	   !loc1.equals(loc2));
  }

  public void testNotEqual() throws URISyntaxException {
    URILocator loc1 = new URILocator("http://www.ontopia.net");    
    assertFalse("URILocator equals null",
	   loc1.equals(null));
  }

  public void testEqualCapsHost() throws URISyntaxException {
    assertEquals("URILocator equals is not equal when host has caps",
            new URILocator("http://www.ONTOPIA.net"), new URILocator("http://www.ontopia.net"));
  }

  public void testEqualCapsSchema() throws URISyntaxException {
    assertEquals("URILocator equals is not equal when schema has caps",
            new URILocator("HTTP://www.ontopia.net"), new URILocator("http://www.ontopia.net"));
  }
  
  public void testRelativeURI() {
    verifyIllegal("#foo");
  }

  public void testResolveRelative() {
    resolvesTo("foo:bar", "#foo", "foo:bar#foo");
  }

  // --- constructors

  public void testConstructorNull() throws URISyntaxException {
    try {
      new URILocator((String) null);
      fail("URILocator accepted null argument to constructor"); 
    }
    catch (NullPointerException e) {
    }
  }
  
  // --- internal methods
  
  private void verifyIllegal(String uri) {
    try {
      new URILocator(uri);
      fail("URI '" + uri + "' considered legal");
    }
    catch (URISyntaxException e) {
    }
  }

  private void normalizesTo(String url, String result) {
    try {
      assertEquals(new URILocator(result), new URILocator(url));
    }
    catch (URISyntaxException e) {
      throw new OntopiaRuntimeException("ERROR: " + e, e);
    }
  }

  private void verifyResolveInvalid(String base, String uri) {
    try {
      LocatorIF resolved = new URILocator(base).resolveAbsolute(uri);
      fail("URI '" + uri + "' relative to '" + base + "' considered legal: " + resolved);
    }
    catch (OntopiaRuntimeException e) {
    }
    catch (URISyntaxException e) {
      fail("Base URI '" + base + "' considered illegal");
    }
  }

  private void resolvesTo(String base, String url, String result) {
    try {
      assertEquals(new URILocator(result), new URILocator(base).resolveAbsolute(url));
    }
    catch (URISyntaxException e) {
      fail("IMPOSSIBLE ERROR: " + e);
    }
  }
  
}
