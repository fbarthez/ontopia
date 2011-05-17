<%@ page import="
  java.io.StringWriter,
  net.ontopia.utils.StringifierIF,
  net.ontopia.topicmaps.core.*,
  net.ontopia.topicmaps.nav2.core.*,
  net.ontopia.topicmaps.nav2.utils.NavigatorUtils,
  net.ontopia.topicmaps.nav2.utils.FrameworkUtils,
  net.ontopia.topicmaps.query.core.QueryProcessorIF,
  net.ontopia.topicmaps.query.core.ParsedQueryIF,
  net.ontopia.topicmaps.query.core.QueryResultIF,
  net.ontopia.topicmaps.query.core.InvalidQueryException,
  net.ontopia.topicmaps.query.utils.QueryUtils,
  net.ontopia.topicmaps.query.impl.basic.QueryTracer,
  net.ontopia.topicmaps.query.impl.utils.SimpleQueryTracer,
  net.ontopia.topicmaps.query.parser.*,
  net.ontopia.topicmaps.utils.TopicStringifiers"
%>
<%@ taglib uri='http://psi.ontopia.net/jsp/taglib/logic'     prefix='logic'  %>
<%@ taglib uri='http://psi.ontopia.net/jsp/taglib/template'  prefix='template'  %>
<%@ taglib uri='http://psi.ontopia.net/jsp/taglib/framework' prefix='framework' %>
<framework:response/>

<logic:context tmparam="tm" settm="topicmap">
  <template:insert template='/views/template_%view%.jsp'>
    <template:put name='title' body='true'>[Omnigator] Query results</template:put>

    <template:put name='heading' body='true'>
      <h1 class="boxed">Query results</h1>
    </template:put>

    <template:put name='plugins' body='true'>
      <framework:pluginList separator=" | " group="topicmap"/>
    </template:put>

<%
// ---------------------------------------------------------------
// retrieve configuration
NavigatorApplicationIF navApp = NavigatorUtils.getNavigatorApplication(pageContext);

// Tomcat doesn't handle character encoding in requests properly,
// so we need to do it. See bug #1420.
String charenc = navApp.getConfiguration().getProperty("defaultCharacterEncoding");
if (charenc != null && charenc.trim().equals(""))
  charenc = null;
if (charenc != null)
  request.setCharacterEncoding(charenc); // weird that we need to do this

StringWriter trace = null;
SimpleQueryTracer tracer = null;
if (request.getParameter("trace") != null) {
  trace = new StringWriter();
  tracer = new SimpleQueryTracer(trace);
}

String tmid = request.getParameter("tm");
String query = request.getParameter("query");

String[] variables = null;
long millis = 0;
int rows = 0;
ParsedQueryIF pquery = null;
String error = null;
StringWriter tmp = new StringWriter();

// get the topic maps
TopicMapIF topicmap = navApp.getTopicMapById(tmid);

try {

  UserIF user = FrameworkUtils.getUser(pageContext);
  String model = "complete";
  if (user != null)
    model = user.getModel();
  
  QueryProcessorIF proc = QueryUtils.getQueryProcessor(topicmap);
  
  try {
    pquery = proc.parse(query);
  }
  catch (InvalidQueryException e) {
    error = "<p>Error: " + e.getMessage() + "</p>";
  }
  
  QueryResultIF result = null;
  
  if (pquery != null) {
    StringifierIF str = TopicStringifiers.getDefaultStringifier();
  
    if (tracer != null)
      QueryTracer.addListener(tracer);
    try {
      millis = System.currentTimeMillis();
      result = pquery.execute();
      millis = System.currentTimeMillis() - millis;
    } finally {
      if (tracer != null)
        QueryTracer.removeListener(tracer);
    }
    variables = result.getColumnNames();
    
    if (variables.length == 0) {
      tmp.write("<tr valign='top'>");
      if (result.next()) {
        // result: true
        rows++;
        tmp.write("<td><i>TRUE</i></td>");
      } else {
        // result: false
        tmp.write("<td><i>FALSE</i></td>");
      }
      tmp.write("</tr>");
    } else {
      while (result.next()) {
        tmp.write("<tr valign='top'>");
        rows++;
        Object[] row = result.getValues();
        for (int ix = 0; ix < variables.length; ix++) {
          if (row[ix] == null)
            tmp.write("<td><i>null</i></td>");
          else if (row[ix] instanceof TopicIF)
            tmp.write("<td><a href=\"../../models/topic_" + model + ".jsp?tm=" + tmid  + "&id=" + ((TopicIF) row[ix]).getObjectId() + "\">" + str.toString(row[ix]) + "</a></td>");
          else if (row[ix] instanceof AssociationIF) {
            String objid = ((AssociationIF) row[ix]).getObjectId();
            tmp.write("<td><a href=\"../../models/association_" + model + ".jsp?tm=" + tmid  + "&id=" + objid + "\">Association " + objid + "</a></td>");
          } else
            tmp.write("<td>" + row[ix] + "</td>");
        }
        tmp.write("</tr>");
      }
    }
  }

} finally {
  navApp.returnTopicMap(topicmap);
}
%>

<template:put name="navigation" body="true">

<p><b>Query:</b><br>
<pre><%= query %></pre></p>

<table>
<tr><th align=left>Execution time</th> <td><%= millis %> millisecs</td></tr>
<tr><th align=left>Result rows</th>    <td><%= rows %></td></tr>
</table>

<% if (pquery != null && pquery == null) { %>
  <p><b>Parsed query:</b></p>

  <p><%= pquery %></p>
<% } %>

</template:put>


<template:put name="content" body="true">

<% if (pquery != null) { %>

<table class="text" border="1" cellpadding="2">
<tr><%
  if (variables != null) {
    for (int ix = 0; ix < variables.length; ix++)
      out.write("<th>" + variables[ix] + "</th>");
  }
%></tr>

<%= tmp %>
</table>

<%
  } else if (error != null) {
%><p><%= error %></p><%
  }
%>

<% if (tracer != null) { %>
  <p>&nbsp;</p>
  <h2>Query trace</h2>
  <pre>
  <%= trace %>
  </pre>
<% } %>


</template:put>

  <template:put name='outro' body='true'></template:put>

  <%-- ============== Outsourced application wide standards ============== --%>
  <template:put name='application' content='/fragments/application.jsp'/>
  <template:put name='header-tagline' content='/fragments/tagline-header.jsp'/>
  <template:put name='footer-tagline' content='/fragments/tagline-footer.jsp'/>
</template:insert>
</logic:context>