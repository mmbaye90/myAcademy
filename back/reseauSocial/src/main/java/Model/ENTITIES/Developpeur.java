package Model.ENTITIES;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class Developpeur {
	private int id_developpeur;
    private String nom_developpeur;
    private Date date_edition;
}
