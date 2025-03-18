package es.iesjandula.reaktor_projector_server.entities;

import es.iesjandula.reaktor_projector_server.entities.ids.CommandId;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(CommandId.class)
public class Command
{
    @Id
    @ManyToOne
    @JoinColumn(name = "modelName")
    private ProjectorModel modelName;

    @Id
    @ManyToOne
    @JoinColumn(name = "actionName")
    private Action action;

    @Id
    private String command;

    @Override
    public String toString()
    {
        return new StringBuilder()
                .append("CommandID - action: ")
                .append(this.action == null ? "N/A" : this.action.getActionName())
                .append(" | modelName: ")
                .append(this.modelName == null ? "N/A" : this.modelName.getModelName())
                .append(" | command: ")
                .append(this.command == null ? "N/A" : this.command)
                .toString();
    }
}
