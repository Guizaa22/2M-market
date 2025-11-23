package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe POJO pour l'entité Vente
 */
public class Vente {
    private int id;
    private LocalDateTime dateVente;
    private BigDecimal totalVente;
    private int utilisateurId;
    private List<DetailVente> details;
    
    // Constructeurs
    public Vente() {
        this.details = new ArrayList<>();
    }
    
    public Vente(LocalDateTime dateVente, BigDecimal totalVente, int utilisateurId) {
        this.dateVente = dateVente;
        this.totalVente = totalVente;
        this.utilisateurId = utilisateurId;
        this.details = new ArrayList<>();
    }
    
    public Vente(int id, LocalDateTime dateVente, BigDecimal totalVente, int utilisateurId) {
        this.id = id;
        this.dateVente = dateVente;
        this.totalVente = totalVente;
        this.utilisateurId = utilisateurId;
        this.details = new ArrayList<>();
    }
    
    // Getters et Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public LocalDateTime getDateVente() {
        return dateVente;
    }
    
    public void setDateVente(LocalDateTime dateVente) {
        this.dateVente = dateVente;
    }
    
    public BigDecimal getTotalVente() {
        return totalVente;
    }
    
    public void setTotalVente(BigDecimal totalVente) {
        this.totalVente = totalVente;
    }
    
    public int getUtilisateurId() {
        return utilisateurId;
    }
    
    public void setUtilisateurId(int utilisateurId) {
        this.utilisateurId = utilisateurId;
    }
    
    public List<DetailVente> getDetails() {
        return details;
    }
    
    public void setDetails(List<DetailVente> details) {
        this.details = details;
    }
    
    public void addDetail(DetailVente detail) {
        this.details.add(detail);
    }
    
    @Override
    public String toString() {
        return "Vente{" +
                "id=" + id +
                ", dateVente=" + dateVente +
                ", totalVente=" + totalVente +
                ", utilisateurId=" + utilisateurId +
                '}';
    }
}

