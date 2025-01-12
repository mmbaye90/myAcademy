package Model.ENTITIES;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Commande {
	private int id_commande;
    private Date date_commande;
    private int total;
    private int id_utilisateur;
}
