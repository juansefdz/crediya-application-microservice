package co.com.pragma.model.notification;
import co.com.pragma.model.NotificationStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Notification {

    private String id;
    private String idSolicitud;
    private String destinatarioEmail;
    private String nombreDestinatario;
    private String asunto;
    private String cuerpoMensaje;
    private NotificationStatus estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

}