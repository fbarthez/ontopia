<%@ taglib uri="/WEB-INF/jsp/webed-form.tld" prefix="webed" %>
<%@ taglib uri="/WEB-INF/jsp/tolog.tld" prefix="tolog"     %>

<tolog:context topicmap="test.ltm">
<webed:form actiongroup="testActionGroup" target="testTarget">
 <webed:button action="testDefaultForward" text="Test" />
</webed:form>
</tolog:context>
