package Model.ENTITIES;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Evaluation {
	private int id_evaluation;
    private int note;
    private int id_utilisateur;
    private int id_jeux;
}
