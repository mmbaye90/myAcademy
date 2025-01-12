package Model.ENTITIES;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Detail {
	private int id_detail;
    private int quantite;
    private double prix;
    private int id_jeux;
    private int id_commande;
}
