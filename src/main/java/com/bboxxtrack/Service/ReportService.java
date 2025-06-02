//package com.bboxxtrack.Service;
//
//import com.bboxxtrack.Model.Report;
//import com.bboxxtrack.Model.Ticket;
//import com.bboxxtrack.Model.TicketComment;
//import com.bboxxtrack.Repository.ReportRepository;
//import com.bboxxtrack.Repository.TicketCommentRepository;
//import com.bboxxtrack.Repository.TicketRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.time.Duration;
//import java.util.List;
//
//@Service
//public class ReportService {
//    @Autowired
//    private ReportRepository reportRepository;
//    @Autowired private TicketRepository ticketRepo;
//    @Autowired private TicketCommentRepository commentRepo;
//
//    public Report saveReport(Report report) {
//        return reportRepository.save(report);
//    }
//
//    public List<Report> getAllReports() {
//        return reportRepository.findAll();
//    }
//
//    public Report getReportById(Long id) {
//        return reportRepository.findById(id).orElse(null);
//    }
//
//    public void deleteReport(Long id) {
//        reportRepository.deleteById(id);
//    }
//
//    public long countOpen() {
//        return ticketRepo.findByStatus("OPEN").size()
//                + ticketRepo.findByStatus("IN_PROGRESS").size();
//    }
//
//    public double avgResponseHours() {
//        List<Ticket> all = ticketRepo.findAll();
//        return all.stream().mapToDouble(t -> {
//            List<TicketComment> c = commentRepo.findByTicket_IdOrderByCreatedAtAsc(t.getId());
//            if(c.isEmpty()) return Double.NaN;
//            Duration d = Duration.between(t.getCreatedAt(), c.get(0).getCreatedAt());
//            return d.toHours();
//        }).filter(h -> !Double.isNaN(h)).average().orElse(0.0);
//    }
//
//    public double avgResolutionHours() {
//        return ticketRepo.findByStatus("CLOSED").stream()
//                .mapToDouble(t -> {
//                    Duration d = Duration.between(t.getCreatedAt(), t.getUpdatedAt());
//                    return d.toHours();
//                })
//                .average().orElse(0.0);
//    }
//
//    public long slaBreaches(long hoursThreshold) {
//        return ticketRepo.findAll().stream()
//                .filter(t -> Duration.between(t.getCreatedAt(), java.time.LocalDateTime.now()).toHours() > hoursThreshold)
//                .count();
//    }
//}
