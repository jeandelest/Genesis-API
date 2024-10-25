package fr.insee.genesis.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SurveyUnitId {

	@JsonProperty("idUE")
	private String idUE;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		SurveyUnitId that = (SurveyUnitId) o;
		return Objects.equals(idUE, that.idUE);
	}

	@Override
	public int hashCode() {
		return Objects.hash(idUE);
	}
}
