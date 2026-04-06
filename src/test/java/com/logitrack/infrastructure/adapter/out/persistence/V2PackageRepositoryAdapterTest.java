package com.logitrack.infrastructure.adapter.out.persistence;

import com.logitrack.application.dto.CreatePackageCommand;
import com.logitrack.application.dto.AddLocationCommand;
import com.logitrack.application.dto.PackageResponse;
import com.logitrack.application.factory.PackageFactory;
import com.logitrack.application.service.PackageService;
import com.logitrack.application.service.PackageServiceImpl;
import com.logitrack.domain.event.DomainEvent;
import com.logitrack.domain.exception.PackageNotFoundException;
import com.logitrack.domain.model.*;
import com.logitrack.domain.model.Package;
import com.logitrack.domain.port.out.EventPublisher;
import com.logitrack.domain.port.out.LocationService;
import com.logitrack.domain.port.out.PackageRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("PackageService - Unit Tests Versión 2")
@MockitoSettings(strictness = Strictness.LENIENT)
class V2PackageRepositoryAdapterTest {

    @Mock
    private PackageRepository packageRepository;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private LocationService locationService;

    @Mock
    private PackageFactory packageFactory;

    @InjectMocks
    private PackageServiceImpl packageService;

    @Captor
    private ArgumentCaptor<Package> packageCaptor;

    @Captor
    private ArgumentCaptor<String> stringCaptor;

    @Captor
    private ArgumentCaptor<DomainEvent> eventCaptor;

    private CreatePackageCommand validCommand;
    private Package validPackage;
    private final String packageId = "LT-001";

    @BeforeEach
    void setUp() {
        // CreatePackageCommand con todos los campos obligatorios
        validCommand = CreatePackageCommand.builder()
                .recipientName("Darwin T")
                .recipientEmail("darwin.tangarife@udea.edu.co")
                .recipientPhone("+573001234567")
                .street("Av. Vegas")
                .city("Sabaneta")
                .state("Ant")
                .country("COL")
                .postalCode("1046520")
                .height(20.0)
                .width(15.0)
                .depth(10.0)
                .weight(2.5)
                .build();

        // Package de dominio equivalente
        validPackage = buildPackageWithStatus(PackageStatus.CREATED);
    }

    //  TESTS SIMPLES

    @Nested
    @DisplayName("Simple Tests — createPackage")
    class CreatePackageTests {

        /**
         * TEST 1
         * Flujo feliz: el factory crea el Package, el repositorio lo guarda
         * y el servicio retorna un PackageResponse con estado CREATED.
         */
        @Test
        @DisplayName("Should create package and return PackageResponse with status CREATED")
        void shouldCreatePackageSuccessfully() {
            // Arrange
            when(locationService.validateLocation("Sabaneta", "COL")).thenReturn(true);
            when(packageFactory.createPackage(validCommand)).thenReturn(validPackage);
            when(packageRepository.save(any(Package.class))).thenReturn(validPackage);

            // Act
            PackageResponse result = packageService.createPackage(validCommand);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo(PackageStatus.CREATED);
            assertThat(result.getRecipient().getEmail())
                    .isEqualTo("darwin.tangarife@udea.edu.co");

            verify(packageRepository, times(1)).save(any(Package.class));
            verify(packageFactory, times(1)).createPackage(validCommand);
        }

        /**
         * TEST 2
         * Si el factory lanza excepción, el repositorio NUNCA debe ser llamado.
         */
        @Test
        @DisplayName("Should NOT save package when factory throws exception")
        void shouldNotSaveWhenFactoryFails() {
            // Arrange
            when(locationService.validateLocation(anyString(), anyString())).thenReturn(true);
            when(packageFactory.createPackage(any())).thenThrow(new RuntimeException("Factory error"));

            // Act & Assert
            assertThatThrownBy(() -> packageService.createPackage(validCommand))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Factory error");

            verifyNoInteractions(packageRepository); // NUNCA se llamó save
        }
    }

    @Nested
    @DisplayName("Simple Tests — findById / getByIdOrThrow")
    class FindByIdTests {

        /**
         * TEST 3
         * findById con ID existente retorna Optional con PackageResponse.
         */
        @Test
        @DisplayName("Should return PackageResponse when package exists")
        void shouldReturnPackageWhenExists() {
            // Arrange
            when(packageRepository.findByIdAndNotDeleted(packageId))
                    .thenReturn(Optional.of(validPackage));

            // Act
            Optional<PackageResponse> result = packageService.findById(packageId);

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(validPackage.getId().getValue());
            verify(packageRepository, times(1)).findByIdAndNotDeleted(packageId);
        }

        /**
         * TEST 5
         * findById con ID inexistente retorna Optional.empty().
         */
        @Test
        @DisplayName("Should return empty Optional when package not found")
        void shouldReturnEmptyWhenNotFound() {
            // Arrange
            when(packageRepository.findByIdAndNotDeleted(anyString()))
                    .thenReturn(Optional.empty());

            // Act
            Optional<PackageResponse> result = packageService.findById("LT-xxx");

            // Assert
            assertThat(result).isEmpty();
        }

        /**
         * TEST 6
         * getByIdOrThrow con ID inexistente lanza PackageNotFoundException.
         */
        @Test
        @DisplayName("Should throw PackageNotFoundException when package not found in getByIdOrThrow")
        void shouldThrowWhenPackageNotFoundInGetByIdOrThrow() {
            // Arrange
            when(packageRepository.findByIdAndNotDeleted(packageId))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> packageService.getByIdOrThrow(packageId))
                    .isInstanceOf(PackageNotFoundException.class)
                    .hasMessageContaining(packageId);

            verify(packageRepository, times(1)).findByIdAndNotDeleted(packageId);
        }
    }

    @Nested
    @DisplayName("Simple Tests — deletePackage")
    class DeletePackageTests {

        /**
         * TEST 7
         * Soft delete de paquete existente save() es llamado con isDeleted=true.
         */
        @Test
        @DisplayName("Should soft delete package successfully")
        void shouldSoftDeletePackage() {
            // Arrange
            when(packageRepository.findByIdAndNotDeleted(packageId))
                    .thenReturn(Optional.of(validPackage));
            when(packageRepository.save(any())).thenReturn(validPackage);

            // Act
            packageService.deletePackage(packageId);

            // Assert
            verify(packageRepository).save(packageCaptor.capture());
            assertThat(packageCaptor.getValue().isDeleted()).isTrue();
        }

        /**
         * TEST 8
         * Intentar eliminar paquete inexistente lanza PackageNotFoundException.
         */
        @Test
        @DisplayName("Should throw PackageNotFoundException when deleting non-existent package")
        void shouldThrowWhenDeletingNonExistentPackage() {
            // Arrange
            when(packageRepository.findByIdAndNotDeleted(packageId))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> packageService.deletePackage(packageId))
                    .isInstanceOf(PackageNotFoundException.class);

            verify(packageRepository, never()).save(any());
        }

        /**
         * TEST 9
         * Si el paquete existe pero falla el save (ej: error de BD),
         * se debe propagar la excepción.
         */
        @Test
        @DisplayName("Should propagate exception when save fails during soft delete")
        void shouldThrowWhenSaveFailsDuringSoftDelete() {
            // Arrange
            when(packageRepository.findByIdAndNotDeleted(packageId))
                    .thenReturn(Optional.of(validPackage));

            doThrow(new RuntimeException("Database error"))
                    .when(packageRepository).save(any(Package.class));

            // Act & Assert
            assertThatThrownBy(() -> packageService.deletePackage(packageId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Database error");

            // Verificamos que sí intentó guardar
            verify(packageRepository).save(any(Package.class));
        }
    }

    //  TESTS COMPLEJOS

    @Nested
    @DisplayName("Complex Tests — updateStatus flow")
    class UpdateStatusTests {

        /**
         * TEST 1
         * Flujo completo CREATED → IN_TRANSIT → OUT_FOR_DELIVERY → DELIVERED.
         * Verifica estados intermedios capturados con ArgumentCaptor.
         */
        @Test
        @DisplayName("Should complete full delivery flow CREATED → DELIVERED")
        void shouldCompleteFullDeliveryFlow() {
            // Arrange
            Package pkg = buildPackageWithStatus(PackageStatus.CREATED);
            when(packageRepository.findByIdAndNotDeleted(pkg.getId().getValue()))
                    .thenReturn(Optional.of(pkg));
            when(packageRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            // ↑ thenAnswer: devuelve el mismo objeto modificado (simula persistencia real)

            String pkgId = pkg.getId().getValue();

            // Act — tres transiciones
            PackageResponse transitResponse = packageService.updateStatus(pkgId, PackageStatus.IN_TRANSIT);
            PackageResponse outForDeliveryResponse =packageService.updateStatus(pkgId, PackageStatus.OUT_FOR_DELIVERY);
            PackageResponse deliveredResponse = packageService.updateStatus(pkgId, PackageStatus.DELIVERED);

            // Assert — save fue llamado 3 veces, capturamos cada versión
            verify(packageRepository, times(3)).save(packageCaptor.capture());

            assertThat(transitResponse.getStatus()).isEqualTo(PackageStatus.IN_TRANSIT);
            assertThat(outForDeliveryResponse.getStatus()).isEqualTo(PackageStatus.OUT_FOR_DELIVERY);
            assertThat(deliveredResponse.getStatus()).isEqualTo(PackageStatus.DELIVERED);
        }

        /**
         * TEST 2
         * Transición inválida CREATED → DELIVERED debe lanzar excepción
         * y el repositorio NO debe guardar nada.
         */
        @Test
        @DisplayName("Should reject invalid transition CREATED → DELIVERED")
        void shouldRejectInvalidTransition() {
            // Arrange
            Package pkg = buildPackageWithStatus(PackageStatus.CREATED);
            when(packageRepository.findByIdAndNotDeleted(pkg.getId().getValue()))
                    .thenReturn(Optional.of(pkg));

            // Act & Assert
            assertThatThrownBy(() ->
                    packageService.updateStatus(pkg.getId().getValue(), PackageStatus.DELIVERED))
                    .isInstanceOf(com.logitrack.domain.exception.InvalidStateTransitionException.class)
                    .hasMessageContaining("CREATED")
                    .hasMessageContaining("DELIVERED");

            verify(packageRepository, never()).save(any());
        }

        /**
         * TEST 3
         * Reintento tras DELIVERY_FAILED → vuelve a IN_TRANSIT.
         * Usa InOrder para verificar secuencia de llamadas al repositorio.
         */
        @Test
        @DisplayName("Should allow retry after DELIVERY_FAILED")
        void shouldAllowRetryAfterDeliveryFailed() {
            // Arrange
            Package pkg = buildPackageWithStatus(PackageStatus.OUT_FOR_DELIVERY);
            String pkgId = pkg.getId().getValue();

            when(packageRepository.findByIdAndNotDeleted(pkgId))
                    .thenReturn(Optional.of(pkg));
            when(packageRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            // Act
            packageService.updateStatus(pkgId, PackageStatus.DELIVERY_FAILED);
            packageService.updateStatus(pkgId, PackageStatus.IN_TRANSIT);

            // Assert — orden garantizado con InOrder
            InOrder inOrder = inOrder(packageRepository);
            inOrder.verify(packageRepository, times(2)).findByIdAndNotDeleted(pkgId);
            inOrder.verify(packageRepository, atLeastOnce()).save(packageCaptor.capture());

            // El último estado guardado debe ser IN_TRANSIT
            assertThat(pkg.getStatus()).isEqualTo(PackageStatus.IN_TRANSIT);
        }
    }

    @Nested
    @DisplayName("Complex Tests — EventPublisher")
    class EventPublisherTests {

        /**
         * TEST 4
         * Al crear un paquete, los DomainEvents deben ser publicados en el topic correcto.
         * Usa ArgumentCaptor para inspeccionar los eventos publicados.
         */
        @Test
        @DisplayName("Should publish domain events after createPackage")
        void shouldPublishEventsAfterCreate() {
            // Arrange
            when(locationService.validateLocation(anyString(), anyString())).thenReturn(true);
            when(packageFactory.createPackage(any())).thenReturn(validPackage);
            when(packageRepository.save(any())).thenReturn(validPackage);

            // Act
            packageService.createPackage(validCommand);

            // Assert — eventPublisher.publish() fue llamado con el topic correcto
            verify(eventPublisher, atLeastOnce())
                    .publish(stringCaptor.capture(), eventCaptor.capture());

            assertThat(stringCaptor.getAllValues())
                    .allMatch(topic -> topic.equals("package-events"));
        }

        /**
         * TEST 5
         * Si eventPublisher falla, el servicio NO debe propagar la excepción
         * (la publicación de eventos es best-effort según el código fuente).
         */
        @Test
        @DisplayName("Should NOT propagate exception when event publishing fails")
        void shouldNotPropagateEventPublishingException() {
            // Arrange
            when(locationService.validateLocation(anyString(), anyString())).thenReturn(true);
            when(packageFactory.createPackage(any())).thenReturn(validPackage);
            when(packageRepository.save(any())).thenReturn(validPackage);
            doThrow(new RuntimeException("Kafka down"))
                    .when(eventPublisher).publish(anyString(), any(DomainEvent.class));

            // Act & Assert — NO debe lanzar excepción al caller
            assertThatCode(() -> packageService.createPackage(validCommand))
                    .doesNotThrowAnyException();
        }
    }

        /**
         * TEST 6
         * Si no se pasan coordenadas, se usa city/country del comando directamente
         * y se valida con LocationService.validateLocation.
         */
        @Test
        @DisplayName("Should use city/country directly when no coordinates provided")
        void shouldUseCityCountryWhenNoCoordinates() {
            // Arrange
            String pkgId = validPackage.getId().getValue();
            AddLocationCommand cmd = AddLocationCommand.builder()
                    .packageId(pkgId)
                    .city("Bogotá")
                    .country("COL")
                    .description("Hub central")
                    .build(); // sin latitude/longitude

            when(packageRepository.findByIdAndNotDeleted(pkgId))
                    .thenReturn(Optional.of(validPackage));
            when(locationService.validateLocation("Bogotá", "COL")).thenReturn(true);
            when(packageRepository.save(any())).thenReturn(validPackage);

            // Act
            PackageResponse result = packageService.addLocation(cmd);

            // Assert
            assertThat(result).isNotNull();
            verify(locationService).validateLocation("Bogotá", "COL");
            verify(locationService, never()).getLocationByCoordinates(anyDouble(), anyDouble());
        }

    @Nested
    @DisplayName("Complex Tests — searchPackages & findByStatus")
    class SearchTests {

        /**
         * TEST 7
         * searchPackages convierte correctamente los criterios y retorna Page<PackageResponse>.
         */
        @Test
        @DisplayName("Should search packages and map results to PackageResponse page")
        void shouldSearchPackagesAndMapToResponse() {
            // Arrange
            PackageService.SearchCriteria criteria = new PackageService.SearchCriteria(
                    "Darwin", null, PackageStatus.IN_TRANSIT,
                    null, null, false
            );
            Pageable pageable = PageRequest.of(0, 10);
            Page<Package> repoPage = new PageImpl<>(
                    List.of(buildPackageWithStatus(PackageStatus.IN_TRANSIT))
            );

            when(packageRepository.search(any(PackageRepository.SearchCriteria.class), eq(pageable)))
                    .thenReturn(repoPage);

            // Act
            Page<PackageResponse> result = packageService.searchPackages(criteria, pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getStatus())
                    .isEqualTo(PackageStatus.IN_TRANSIT);

            verify(packageRepository).search(any(), eq(pageable));
        }

        /**
         * TEST 8
         * findByStatus retorna lista de PackageResponse para el estado solicitado.
         */
        @Test
        @DisplayName("Should return list of PackageResponse filtered by status")
        void shouldReturnPackagesByStatus() {
            // Arrange
            List<Package> inTransitPackages = List.of(
                    buildPackageWithStatus(PackageStatus.IN_TRANSIT),
                    buildPackageWithStatus(PackageStatus.IN_TRANSIT)
            );
            when(packageRepository.findByStatus(PackageStatus.IN_TRANSIT))
                    .thenReturn(inTransitPackages);

            // Act
            List<PackageResponse> result = packageService.findByStatus(PackageStatus.IN_TRANSIT);

            // Assert
            assertThat(result)
                    .hasSize(2)
                    .allMatch(r -> r.getStatus() == PackageStatus.IN_TRANSIT);
        }
    }

    @Nested
    @DisplayName("Complex Tests — Spy")
    class SpyTests {

        /**
         * TEST 9
         * Usa Spy sobre el Package real para verificar que changeStatus()
         * fue invocado internamente por updateStatus() del servicio.
         */
        @Test
        @DisplayName("Should call changeStatus on real Package object via Spy")
        void shouldCallChangeStatusOnRealPackage() {
            // Arrange — Spy: instancia real + capacidad de verificar llamadas
            Package spyPackage = spy(buildPackageWithStatus(PackageStatus.CREATED));
            String pkgId = spyPackage.getId().getValue();

            when(packageRepository.findByIdAndNotDeleted(pkgId))
                    .thenReturn(Optional.of(spyPackage));
            when(packageRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            // Act
            packageService.updateStatus(pkgId, PackageStatus.IN_TRANSIT);

            // Assert — el método REAL fue llamado con el argumento correcto
            verify(spyPackage).changeStatus(PackageStatus.IN_TRANSIT);
            assertThat(spyPackage.getStatus()).isEqualTo(PackageStatus.IN_TRANSIT);
        }
    }

    // =========================================================================
    //  Helper
    // =========================================================================

    /**
     * Construye un Package real del dominio en el estado solicitado,
     * siguiendo las transiciones válidas definidas en el modelo.
     */
    private Package buildPackageWithStatus(PackageStatus target) {
        Recipient.Address address = Recipient.Address.builder()
                .street("Av. Vegas").city("Sabaneta")
                .state("Ant").country("COL").postalCode("1046520").build();

        Recipient recipient = Recipient.builder()
                .name("Darwin T").email("darwin.tangarife@udea.edu.co")
                .phone("+573001234567").address(address).build();

        Package pkg = Package.builder()
                .recipient(recipient)
                .dimensions(Dimensions.of(20, 15, 10))
                .weight(Weight.ofKilograms(2.5))
                .build();

        // Transiciones válidas en orden
        if (target == PackageStatus.CREATED) return pkg;

        pkg.changeStatus(PackageStatus.IN_TRANSIT);
        if (target == PackageStatus.IN_TRANSIT) return pkg;

        pkg.changeStatus(PackageStatus.OUT_FOR_DELIVERY);
        if (target == PackageStatus.OUT_FOR_DELIVERY) return pkg;

        if (target == PackageStatus.DELIVERY_FAILED) {
            pkg.changeStatus(PackageStatus.DELIVERY_FAILED);
            return pkg;
        }

        pkg.changeStatus(PackageStatus.DELIVERED);
        return pkg;
    }

}
