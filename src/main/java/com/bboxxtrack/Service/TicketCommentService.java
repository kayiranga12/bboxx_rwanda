package com.bboxxtrack.Service;

import com.bboxxtrack.Model.TicketComment;
import com.bboxxtrack.Repository.TicketCommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TicketCommentService {
    @Autowired private TicketCommentRepository repo;

    public TicketComment save(TicketComment c){ return repo.save(c); }
    public List<TicketComment> findForTicket(Long ticketId){
        return repo.findByTicket_IdOrderByCreatedAtAsc(ticketId);
    }
}
