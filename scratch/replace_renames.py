import os
import re

# Directory to scan
ROOT_DIR = r"C:\Users\Manuel_Dongo\Music\Importease_Aduanero_Final\src"

# Rename map
renames = {
    # Main Services
    "AduanasService": "AduanasServicio",
    "ImportacionService": "ImportacionServicio",
    "HsCodeDAO": "HsCodeRepositorio",
    "VUCEValidatorService": "VuceValidadorServicio",

    # Services root
    "ComparadorEscenariosService": "ComparadorEscenariosServicio",
    "ContribuyenteService": "ContribuyenteServicio",
    "CorreoRecuperacionService": "CorreoRecuperacionServicio",
    "DataConfidenceService": "DataConfidenceServicio",
    "EventoUsuarioService": "EventoUsuarioServicio",
    "ExternalApiService": "ServicioApiExterna",
    "FuenteEventoService": "FuenteEventoServicio",
    "MatrizRestriccionesService": "MatrizRestriccionesServicio",
    "MentorService": "MentorServicio",
    "NotificacionService": "NotificacionServicio",
    "OperacionAuthorizationService": "OperacionAutorizacionServicio",
    "PdfService": "PdfServicio",
    "ReglasPlazoService": "ReglasPlazoServicio",
    "RestriccionesFormalService": "RestriccionesFormalServicio",
    "RiskScoringService": "RiskScoringServicio",
    "SunatBulkLoaderService": "SunatCargaMasivaServicio",
    "SunatRucService": "SunatRucServicio",
    "VucePamService": "VucePamServicio",

    # Services under aduanas
    "AduanasAlertsService": "AduanasAlertasServicio",
    "AduanasChecklistService": "AduanasListaChequeoServicio",
    "AduanasLegalService": "AduanasLegalServicio",
    "AduanasProcedureCatalog": "AduanasCatalogoProcedimientos",
    "AduanasResponseBuilder": "AduanasConstructorRespuesta",
    "AduanasTimelineService": "AduanasLineaTiempoServicio",
    "AduanasTimelineSimulator": "AduanasSimuladorLineaTiempo",
    "DtaMapper": "DtaMapeador",
    "ManifiestoCommandService": "ManifiestoComandoServicio",
    "ModalidadEvaluatorService": "ModalidadEvaluadorServicio",
    "OperacionAduaneraMapper": "OperacionAduaneraMapeador",
    "PredamMapper": "PredamMapeador",
    "RegimenEvaluatorService": "RegimenEvaluadorServicio",
    "ReimportacionService": "ReimportacionServicio",
    "TransbordoService": "TransbordoServicio",

    # Services under documentos
    "DocumentoResponseBuilder": "DocumentoConstructorRespuesta",
    "DocumentoValidationService": "DocumentoValidacionServicio",

    # Services under login
    "AuthenticationResult": "ResultadoAutenticacion",
    "CaptchaValidationService": "CaptchaValidacionServicio",
    "LoginAttemptService": "LoginIntentoServicio",
    "PasswordHashService": "HashContrasenaServicio",
    "SessionService": "SesionServicio",

    # Repositories under permisos
    "PermisoConsultaRepository": "PermisoConsultaRepositorio",
    "PermisoDocumentoRepository": "PermisoDocumentoRepositorio",
    "PermisoRepositorySupport": "PermisoRepositorioSoporte",
    "PermisoRespuestaRepository": "PermisoRespuestaRepositorio",
    "PermisoSolicitudRepository": "PermisoSolicitudRepositorio",

    # Repositories root
    "PredamValidationRepositorio": "PredamValidacionRepositorio",
    "PredamValidationRepository": "PredamValidacionRepositorio",

    # Repositories under jpa
    "DocumentoPermisoJpaRepository": "DocumentoPermisoJpaRepositorio",
    "EntidadControlJpaRepository": "EntidadControlJpaRepositorio",
    "HsCodeJpaRepository": "HsCodeJpaRepositorio",
    "HsSinonimoJpaRepository": "HsSinonimoJpaRepositorio",
    "ImportacionJpaRepository": "ImportacionJpaRepositorio",
    "OperacionJpaRepository": "OperacionJpaRepositorio",
    "PreguntaPermisoJpaRepository": "PreguntaPermisoJpaRepositorio",
    "ReglaRestriccionJpaRepository": "ReglaRestriccionJpaRepositorio",
    "RespuestaPermisoOperacionJpaRepository": "RespuestaPermisoOperacionJpaRepositorio",
    "SolicitudPermisoDatoJpaRepository": "SolicitudPermisoDatoJpaRepositorio",
    "SolicitudPermisoJpaRepository": "SolicitudPermisoJpaRepositorio",
    "TrackingEnvioJpaRepository": "TrackingEnvioJpaRepositorio",
    "TrackingEventoJpaRepository": "TrackingEventoJpaRepositorio",
    "TrackingSyncLogJpaRepository": "TrackingSyncLogJpaRepositorio",
    "UsuarioJpaRepository": "UsuarioJpaRepositorio",

    # DTOs
    "CoherenciaIssueDTO": "IncidenciaCoherenciaDTO",
    "GuidedStepDTO": "PasoGuiadoDTO",
    "HealthPanelDTO": "PanelSaludDTO",
    "NextActionDTO": "SiguienteAccionDTO",
    "PredamResponseDTO": "RespuestaPredamDTO",
    "LoginRequestDTO": "SolicitudLoginDTO",
    "LoginResponseDTO": "RespuestaLoginDTO",

    # Controllers & request handlers
    "PermisoController": "PermisoControlador",
    "PermisoControllerSupport": "PermisoControladorSoporte",
    "PermisoGetRequestHandler": "PermisoManejadorPeticionGet",
    "PermisoPostRequestHandler": "PermisoManejadorPeticionPost",
    "LoginRequestHandler": "LoginManejadorPeticion",

    # Models
    "ResponseEnvelope": "RespuestaEnvoltorio",

    # General / Previous renames
    "UsuarioDAO": "UsuarioRepositorio",
    "ArancelService": "ArancelServicio",
    "AuditoriaService": "AuditoriaServicio",
    "PredamValidationService": "PredamValidacionServicio",
    "PredamValidationException": "PredamValidacionException",
    "PlazoCriticoService": "PlazoCriticoServicio",
    "TipoCambioService": "TipoCambioServicio",
    "GuidedFlowService": "FlujoGuiadoServicio",
    "NextActionService": "SiguienteAccionServicio",
    "CoherenciaAduaneraService": "CoherenciaAduaneraServicio",
    "DashboardService": "PanelServicio",
    "TlcService": "TratadoLibreComercioServicio",
    "PermisoEvaluationService": "PermisoEvaluacionServicio",
    "PermisoExpedienteService": "PermisoExpedienteServicio",
    "PermisoPdfService": "PermisoPdfServicio",
    "PermisoAuditService": "PermisoAuditoriaServicio",
    "PermisoCommandService": "PermisoComandoServicio",
    "PermisoQueryService": "PermisoConsultaServicio",
    "HealthPanelService": "SaludPanelServicio",
    "AduanasDamService": "AduanasDamServicio",

    # Controllers
    "AduanasController": "AduanasControlador",
    "CostoController": "CostoControlador",
    "FuentesController": "FuentesControlador",
    "HsController": "HsControlador",
    "IncotermController": "IncotermControlador",
    "MentorController": "MentorControlador",
    "ObservatorioController": "ObservatorioControlador",
    "OperacionController": "OperacionControlador",
    "PanelController": "PanelControlador",
    "RiesgoController": "RiesgoControlador",
    "TendenciasController": "TendenciasControlador",
    "TipoCambioController": "TipoCambioControlador",
    "TrackingController": "TrackingControlador",
    "UsuarioController": "UsuarioControlador",
    "VuceController": "VuceControlador",
    "WizardController": "WizardControlador",
    "LoginController": "LoginControlador",
    "LogoutController": "LogoutControlador",
    "RegistroUsuarioController": "RegistroUsuarioControlador",

    # DAOs
    "ImportacionDAO": "ImportacionRepositorio",
    "IncotermDAO": "IncotermRepositorio",
    "ObservatorioDAO": "ObservatorioRepositorio",
    "OperacionDAO": "OperacionRepositorio",
    "PermisoDAO": "PermisoRepositorio",
    "VuceRestriccionDAO": "VuceRestriccionRepositorio",
    "DocumentoRepository": "DocumentoRepositorio",
    "GuidedFlowStepRepository": "FlujoGuiadoPasoRepositorio",
    "PasswordResetTokenRepository": "PasswordResetTokenRepositorio",
    
    # Tests
    "AduanasControllerTest": "AduanasControladorTest",
    "AduanasServiceTest": "AduanasServicioTest",
    "ComparadorEscenariosServiceTest": "ComparadorEscenariosServicioTest",
    "CorreoRecuperacionServiceTest": "CorreoRecuperacionServicioTest",
    "DashboardServiceTest": "PanelServicioTest",
    "DataConfidenceServiceTest": "DataConfidenceServicioTest",
    "GuidedFlowServiceTest": "FlujoGuiadoServicioTest",
    "ImportacionServiceTest": "ImportacionServicioTest",
    "PdfServiceTest": "PdfServicioTest",
    "PredamValidationServiceTest": "PredamValidacionServicioTest",
    "RiskScoringServiceTest": "RiskScoringServicioTest",
    "TipoCambioServiceTest": "TipoCambioServicioTest",
    "VUCEValidatorServiceTest": "VuceValidadorServicioTest",
    "GuidedFlowStepCatalogTest": "FlujoGuiadoPasoCatalogoTest",
    "AuthenticationServiceTest": "AutenticacionServicioTest",
    "DocumentoValidationServiceTest": "DocumentoValidacionServicioTest",
    "GuidedFlowStepCatalog": "FlujoGuiadoPasoCatalogo",
    "AuthenticationService": "AutenticacionServicio"
}

# Variable renames
var_renames = {}
for old_class, new_class in renames.items():
    if old_class[0].isupper() and new_class[0].isupper():
        old_var = old_class[0].lower() + old_class[1:]
        new_var = new_class[0].lower() + new_class[1:]
        var_renames[old_var] = new_var

# Merge both sets
all_renames = {}
# Put longer names first to avoid partial replacements
for k in sorted(renames.keys(), key=len, reverse=True):
    all_renames[k] = renames[k]
for k in sorted(var_renames.keys(), key=len, reverse=True):
    all_renames[k] = var_renames[k]

print(f"Loaded {len(all_renames)} replacement mappings.")

# Walk through directories
modified_count = 0
for root, dirs, files in os.walk(ROOT_DIR):
    for file in files:
        if file.endswith(('.java', '.jsp', '.xml', '.properties')):
            file_path = os.path.join(root, file)
            try:
                with open(file_path, 'r', encoding='utf-8') as f:
                    content = f.read()
            except UnicodeDecodeError:
                try:
                    with open(file_path, 'r', encoding='iso-8859-1') as f:
                        content = f.read()
                except Exception as e:
                    print(f"Could not read file {file_path}: {e}")
                    continue
            except Exception as e:
                print(f"Could not read file {file_path}: {e}")
                continue

            orig_content = content
            # Replace occurrences
            for old_word, new_word in all_renames.items():
                content = re.sub(r'\b' + re.escape(old_word) + r'\b', new_word, content)

            if content != orig_content:
                try:
                    with open(file_path, 'w', encoding='utf-8') as f:
                        f.write(content)
                    modified_count += 1
                except Exception as e:
                    print(f"Could not write file {file_path}: {e}")

print(f"Updated {modified_count} files.")
