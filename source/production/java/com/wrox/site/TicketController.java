package com.wrox.site;

import java.util.List;
import javax.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequestMapping("ticket")
public class TicketController
{
    private static final Logger log = LogManager.getLogger();

    private volatile long TICKET_ID_SEQUENCE = 1;

    private Map<Long, Ticket> ticketDatabase = new LinkedHashMap<>();

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        log.debug("GET request received.");
        String action = request.getParameter("action");
        if(action == null)
            action = "list";
        switch(action)
        {
            case "create":
                this.showTicketForm(request, response);
                break;
            case "view":
//                this.viewTicket(request, response);
                break;
            case "download":
                this.downloadAttachment(request, response);
                break;
            case "list":
            default:
//                this.listTickets(request, response);
                break;
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        log.debug("POST request received.");
        String action = request.getParameter("action");
        if(action == null)
            action = "list";
        switch(action)
        {
            case "create":
//                this.createTicket(request, response);
                break;
            case "list":
            default:
                response.sendRedirect("tickets");
                break;
        }
    }

    private void showTicketForm(HttpServletRequest request,
                                HttpServletResponse response)
            throws ServletException, IOException
    {
        request.getRequestDispatcher("/WEB-INF/jsp/view/add.jsp")
               .forward(request, response);
    }

    @RequestMapping(value = "view/{ticketId}", method = RequestMethod.GET)
    public ModelAndView view(Map<String, Object> model,
            @PathVariable("ticketId") long ticketId)
    {
        Ticket ticket = this.ticketDatabase.get(ticketId);
        if(ticket == null)
            return this.getListRedirectModelAndView();
        model.put("ticketId", Long.toString(ticketId));
        model.put("ticket", ticket);
        return new ModelAndView("ticket/view");
    }

    private void downloadAttachment(HttpServletRequest request,
                                    HttpServletResponse response)
            throws ServletException, IOException
    {
        String idString = request.getParameter("ticketId");
        log.entry(idString);
        Ticket ticket = this.getTicket(idString, response);
        if(ticket == null)
            return;

        String name = request.getParameter("attachment");
        if(name == null)
        {
            response.sendRedirect("tickets?action=view&ticketId=" + idString);
            return;
        }

        Attachment attachment = ticket.getAttachment(name);
        if(attachment == null)
        {
            log.info("Requested attachment {} not found on ticket {}.", name, idString);
            response.sendRedirect("tickets?action=view&ticketId=" + idString);
            return;
        }

        response.setHeader("Content-Disposition",
                "attachment; filename=" + attachment.getName());
        response.setContentType("application/octet-stream");

        ServletOutputStream stream = response.getOutputStream();
        stream.write(attachment.getContents());
        log.exit();
    }

    @RequestMapping(value = {"", "list"}, method = RequestMethod.GET)
    public String list(Map<String, Object> model)
    {
        log.debug("Listing tickets.");
        model.put("ticketDatabase", this.ticketDatabase);

        return "ticket/list";
    }

    @RequestMapping(value = "create", method = RequestMethod.POST)
    public View create(HttpSession session, Form form) throws IOException
    {
        Ticket ticket = new Ticket();
        ticket.setId(this.getNextTicketId());
        ticket.setCustomerName((String)session.getAttribute("username"));
        ticket.setSubject(form.getSubject());
        ticket.setBody(form.getBody());
        ticket.setDateCreated(Instant.now());

        for(MultipartFile filePart : form.getAttachments())
        {
            log.debug("Processing attachment for new ticket.");
            Attachment attachment = new Attachment();
            attachment.setName(filePart.getOriginalFilename());
            attachment.setMimeContentType(filePart.getContentType());
            attachment.setContents(filePart.getBytes());
            if((attachment.getName() != null && attachment.getName().length() > 0) ||
                    (attachment.getContents() != null && attachment.getContents().length > 0))
                ticket.addAttachment(attachment);
        }

        this.ticketDatabase.put(ticket.getId(), ticket);

        return new RedirectView("/ticket/view/" + ticket.getId(), true, false);
    }

    private Attachment processAttachment(Part filePart)
            throws IOException
    {
        log.entry(filePart);
        InputStream inputStream = filePart.getInputStream();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        int read;
        final byte[] bytes = new byte[1024];

        while((read = inputStream.read(bytes)) != -1)
        {
            outputStream.write(bytes, 0, read);
        }

        Attachment attachment = new Attachment();
        attachment.setName(filePart.getSubmittedFileName());
        attachment.setContents(outputStream.toByteArray());

        return log.exit(attachment);
    }

    private Ticket getTicket(String idString, HttpServletResponse response)
            throws ServletException, IOException
    {
        log.entry(idString);
        if(idString == null || idString.length() == 0)
        {
            response.sendRedirect("tickets");
            return null;
        }

        try
        {
            Ticket ticket = this.ticketDatabase.get(Integer.parseInt(idString));
            if(ticket == null)
            {
                response.sendRedirect("tickets");
                return log.exit(null);
            }
            return log.exit(ticket);
        }
        catch(Exception e)
        {
            response.sendRedirect("tickets");
            return log.exit(null);
        }
    }

    private synchronized long getNextTicketId()
    {
        return this.TICKET_ID_SEQUENCE++;
    }

    private ModelAndView getListRedirectModelAndView()
    {
        return new ModelAndView(this.getListRedirectView());
    }

    private View getListRedirectView()
    {
        return new RedirectView("/ticket/list", true, false);
    }


    public static class Form
    {
        private String subject;
        private String body;
        private List<MultipartFile> attachments;

        public String getSubject()
        {
            return subject;
        }

        public void setSubject(String subject)
        {
            this.subject = subject;
        }

        public String getBody()
        {
            return body;
        }

        public void setBody(String body)
        {
            this.body = body;
        }

        public List<MultipartFile> getAttachments()
        {
            return attachments;
        }

        public void setAttachments(List<MultipartFile> attachments)
        {
            this.attachments = attachments;
        }
    }
}
