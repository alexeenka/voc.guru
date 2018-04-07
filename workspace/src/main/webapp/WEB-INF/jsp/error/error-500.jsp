<%--
  User: aalexeenka
  Date: 23.12.2016
  Time: 12:59
--%>

<%@ page isErrorPage="true" contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="static guru.h4t_eng.util.ErrorUtils.produceErrorTicket" %>
<%@ page import="static guru.h4t_eng.rest.Main4Rest.getUserId" %>
<%@ page import="java.util.Optional" %>
<%@ page import="java.util.UUID" %>
<%@ page session="false" %>

<html>
<head><title></title></head>
<body>

<%!
private static final Logger LOG = LoggerFactory.getLogger("H4TLog.error-500-jsp");
%>

<%
    final String errorTicket = produceErrorTicket();
    final String errorTicketMsg = "Ticket: " + errorTicket + ".";

    // Add info to log
    {
        String logMsg = errorTicketMsg;
        final Optional<UUID> userId = Optional.ofNullable(getUserId(request));
        if (userId.isPresent()) logMsg += " UserId: " + userId.get();

        LOG.error(logMsg);
        LOG.error("Error [" + errorTicket + "]", exception);
    }
%>
<%-- Print for user --%>
<div>
    <span style="font-weight:bold">Ticket:&nbsp;</span><span><%=errorTicket%></span>
</div>
<div>
    <span style="font-weight:bold">Error:&nbsp;</span><span><%=exception.getMessage()%></span>
</div>

</body>
</html>
