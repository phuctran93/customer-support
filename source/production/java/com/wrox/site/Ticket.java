package com.wrox.site;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.wrox.validation.NotBlank;
import javax.validation.Valid;
import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Ticket
{
    private long id;

    @NotBlank(message = "{validate.ticket.customerName}")
    private String customerName;

    @NotBlank(message = "{validate.ticket.subject}")
    private String subject;

    @NotBlank(message = "{validate.ticket.body}")
    private String body;

    private Instant dateCreated;

    @Valid
    private Map<String, Attachment> attachments = new LinkedHashMap<>();

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public String getCustomerName()
    {
        return customerName;
    }

    public void setCustomerName(String customerName)
    {
        this.customerName = customerName;
    }

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

    public Instant getDateCreated()
    {
        return dateCreated;
    }

    public void setDateCreated(Instant dateCreated)
    {
        this.dateCreated = dateCreated;
    }

    @JsonIgnore
    public Attachment getAttachment(String name)
    {
        return this.attachments.get(name);
    }

    public Collection<Attachment> getAttachments()
    {
        return this.attachments.values();
    }

    @JsonIgnore
    public void addAttachment(Attachment attachment)
    {
        this.attachments.put(attachment.getName(), attachment);
    }

    public void setAttachments(List<Attachment> attachments)
    {
        for(Attachment attachment : attachments)
            this.addAttachment(attachment);
    }

    @JsonIgnore
    public int getNumberOfAttachments()
    {
        return this.attachments.size();
    }
}
