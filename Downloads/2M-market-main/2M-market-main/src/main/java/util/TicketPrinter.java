package util;

import model.DetailVente;
import model.Vente;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Classe utilitaire pour imprimer les tickets de vente
 */
public class TicketPrinter {
    
    /**
     * Imprime un ticket de vente
     * @param vente La vente à imprimer
     * @param details La liste des détails de vente
     */
    public static void imprimerTicket(Vente vente, List<DetailVente> details) {
        try {
            // Créer le contenu du ticket
            StringBuilder ticket = new StringBuilder();
            ticket.append("================================\n");
            ticket.append("     2M MARKET - TICKET\n");
            ticket.append("================================\n");
            ticket.append("Date: ").append(vente.getDateVente().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))).append("\n");
            ticket.append("Ticket N°: ").append(vente.getId()).append("\n");
            ticket.append("--------------------------------\n");
            ticket.append("ARTICLES:\n");
            ticket.append("--------------------------------\n");
            
            BigDecimal total = BigDecimal.ZERO;
            for (DetailVente detail : details) {
                BigDecimal sousTotal = detail.getPrixVenteUnitaire().multiply(new BigDecimal(detail.getQuantite()));
                total = total.add(sousTotal);
                ticket.append(String.format("%-20s %2dx %6.2f€ = %6.2f€\n",
                    truncate(detail.getProduit().getNom(), 20),
                    detail.getQuantite(),
                    detail.getPrixVenteUnitaire().doubleValue(),
                    sousTotal.doubleValue()));
            }
            
            ticket.append("--------------------------------\n");
            ticket.append(String.format("TOTAL: %26.2f€\n", total.doubleValue()));
            ticket.append("================================\n");
            ticket.append("     MERCI DE VOTRE VISITE\n");
            ticket.append("================================\n");
            
            // Sauvegarder dans un fichier (simulation impression)
            String filename = "ticket_" + vente.getId() + "_" + 
                vente.getDateVente().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".txt";
            
            try (FileWriter writer = new FileWriter(filename)) {
                writer.write(ticket.toString());
            }
            
            System.out.println("Ticket imprimé: " + filename);
            System.out.println(ticket.toString());
            
        } catch (IOException e) {
            System.err.println("Erreur lors de l'impression du ticket: " + e.getMessage());
        }
    }
    
    /**
     * Tronque une chaîne à une longueur maximale
     */
    private static String truncate(String str, int maxLength) {
        if (str == null) return "";
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength - 3) + "...";
    }
}

