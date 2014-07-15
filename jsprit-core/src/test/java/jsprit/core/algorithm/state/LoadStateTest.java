package jsprit.core.algorithm.state;

import jsprit.core.problem.AbstractActivity;
import jsprit.core.problem.Capacity;
import jsprit.core.problem.JobActivityFactory;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import jsprit.core.problem.job.*;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.state.StateFactory;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.problem.vehicle.VehicleType;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests to test correct calc of load states
 */
public class LoadStateTest {

    private VehicleRoute serviceRoute;

    private VehicleRoute pickup_delivery_route;

    private VehicleRoute shipment_route;

    private StateManager stateManager;

    @Before
    public void doBefore(){
        Vehicle vehicle = mock(Vehicle.class);
        VehicleType type = mock(VehicleType.class);
        when(type.getCapacityDimensions()).thenReturn(Capacity.Builder.newInstance().addDimension(0,20).build());
        when(vehicle.getType()).thenReturn(type);

        VehicleRoutingProblem.Builder serviceProblemBuilder = VehicleRoutingProblem.Builder.newInstance();
        Service s1 = Service.Builder.newInstance("s").addSizeDimension(0,10).setLocationId("loc").build();
        Service s2 = Service.Builder.newInstance("s2").addSizeDimension(0,5).setLocationId("loc").build();
        serviceProblemBuilder.addJob(s1).addJob(s2);
        final VehicleRoutingProblem serviceProblem = serviceProblemBuilder.build();

        Pickup pickup = mock(Pickup.class);
        when(pickup.getSize()).thenReturn(Capacity.Builder.newInstance().addDimension(0, 10).build());
        Delivery delivery = mock(Delivery.class);
        when(delivery.getSize()).thenReturn(Capacity.Builder.newInstance().addDimension(0,5).build());

        Shipment shipment1 = mock(Shipment.class);
        when(shipment1.getSize()).thenReturn(Capacity.Builder.newInstance().addDimension(0, 10).build());
        Shipment shipment2 = mock(Shipment.class);
        when(shipment2.getSize()).thenReturn(Capacity.Builder.newInstance().addDimension(0, 5).build());

        VehicleRoute.Builder serviceRouteBuilder = VehicleRoute.Builder.newInstance(vehicle);
        serviceRouteBuilder.setJobActivityFactory(new JobActivityFactory() {

            @Override
            public List<AbstractActivity> createActivity(Job job) {
                return serviceProblem.copyAndGetActivities(job);
            }

        });
        serviceRoute = serviceRouteBuilder.addService(s1).addService(s2).build();
        pickup_delivery_route = VehicleRoute.Builder.newInstance(vehicle).addService(pickup).addService(delivery).build();
        shipment_route = VehicleRoute.Builder.newInstance(vehicle).addPickup(shipment1).addPickup(shipment2).addDelivery(shipment2).addDelivery(shipment1).build();

        stateManager = new StateManager(mock(VehicleRoutingTransportCosts.class));
        stateManager.updateLoadStates();
        stateManager.informInsertionStarts(Arrays.asList(serviceRoute), Collections.<Job>emptyList());
//        <stateManager.informInsertionStarts(Arrays.asList(serviceRoute,pickup_delivery_route,shipment_route), Collections.<Job>emptyList());

    }


    @Test
    public void loadAtEndShouldBe15(){
        Capacity routeState = stateManager.getRouteState(serviceRoute, StateFactory.LOAD_AT_END, Capacity.class);
        assertEquals(15,routeState.get(0));
    }

    @Test
    public void loadAtBeginningShouldBe0(){
        Capacity routeState = stateManager.getRouteState(serviceRoute, StateFactory.LOAD_AT_BEGINNING, Capacity.class);
        assertEquals(0,routeState.get(0));
    }

    @Test
    public void loadAtAct1ShouldBe10(){
        Capacity atAct1 = stateManager.getActivityState(serviceRoute.getActivities().get(0), StateFactory.LOAD, Capacity.class);
        assertEquals(10,atAct1.get(0));
    }

    @Test
    public void loadAtAct2ShouldBe15(){
        Capacity atAct2 = stateManager.getActivityState(serviceRoute.getActivities().get(1), StateFactory.LOAD, Capacity.class);
        assertEquals(15,atAct2.get(0));
    }

    @Test
    public void futureMaxLoatAtAct1ShouldBe15(){
        Capacity atAct1 = stateManager.getActivityState(serviceRoute.getActivities().get(0), StateFactory.FUTURE_MAXLOAD, Capacity.class);
        assertEquals(15,atAct1.get(0));
    }

    @Test
    public void futureMaxLoatAtAct2ShouldBe15(){
        Capacity atAct2 = stateManager.getActivityState(serviceRoute.getActivities().get(1), StateFactory.FUTURE_MAXLOAD, Capacity.class);
        assertEquals(15,atAct2.get(0));
    }

    @Test
    public void pastMaxLoatAtAct1ShouldBe0(){
        Capacity atAct1 = stateManager.getActivityState(serviceRoute.getActivities().get(0), StateFactory.PAST_MAXLOAD, Capacity.class);
        assertEquals(10,atAct1.get(0));
    }

    @Test
    public void pastMaxLoatAtAct2ShouldBe10(){
        Capacity atAct2 = stateManager.getActivityState(serviceRoute.getActivities().get(1), StateFactory.PAST_MAXLOAD, Capacity.class);
        assertEquals(15,atAct2.get(0));
    }

    /*
    test pickup_delivery_route
    pickup 10 and deliver 5
     */
    @Test
    public void when_pdroute_loadAtEndShouldBe10(){
        Capacity routeState = stateManager.getRouteState(pickup_delivery_route, StateFactory.LOAD_AT_END, Capacity.class);
        assertEquals(10,routeState.get(0));
    }

    @Test
    public void when_pdroute_loadAtBeginningShouldBe5(){
        Capacity routeState = stateManager.getRouteState(pickup_delivery_route, StateFactory.LOAD_AT_BEGINNING, Capacity.class);
        assertEquals(5,routeState.get(0));
    }

    @Test
    public void when_pdroute_loadAtAct1ShouldBe15(){
        Capacity atAct1 = stateManager.getActivityState(pickup_delivery_route.getActivities().get(0), StateFactory.LOAD, Capacity.class);
        assertEquals(15,atAct1.get(0));
    }

    @Test
    public void when_pdroute_loadAtAct2ShouldBe10(){
        Capacity atAct2 = stateManager.getActivityState(pickup_delivery_route.getActivities().get(1), StateFactory.LOAD, Capacity.class);
        assertEquals(10,atAct2.get(0));
    }

    @Test
    public void when_pdroute_futureMaxLoatAtAct1ShouldBe15(){
        Capacity atAct1 = stateManager.getActivityState(pickup_delivery_route.getActivities().get(0), StateFactory.FUTURE_MAXLOAD, Capacity.class);
        assertEquals(15,atAct1.get(0));
    }

    @Test
    public void when_pdroute_futureMaxLoatAtAct2ShouldBe10(){
        Capacity atAct2 = stateManager.getActivityState(pickup_delivery_route.getActivities().get(1), StateFactory.FUTURE_MAXLOAD, Capacity.class);
        assertEquals(10,atAct2.get(0));
    }

    @Test
    public void when_pdroute_pastMaxLoatAtAct1ShouldBe15(){
        Capacity atAct1 = stateManager.getActivityState(pickup_delivery_route.getActivities().get(0), StateFactory.PAST_MAXLOAD, Capacity.class);
        assertEquals(15,atAct1.get(0));
    }

    @Test
    public void when_pdroute_pastMaxLoatAtAct2ShouldBe10(){
        Capacity atAct2 = stateManager.getActivityState(pickup_delivery_route.getActivities().get(1), StateFactory.PAST_MAXLOAD, Capacity.class);
        assertEquals(15,atAct2.get(0));
    }

    /*
    shipment_route
    shipment1 10
    shipment2 15
    pick1_pick2_deliver2_deliver1

     */
    @Test
    public void when_shipmentroute_loadAtEndShouldBe0(){
        Capacity routeState = stateManager.getRouteState(shipment_route, StateFactory.LOAD_AT_END, Capacity.class);
        assertEquals(0,routeState.get(0));
    }

    @Test
    public void when_shipmentroute_loadAtBeginningShouldBe0(){
        Capacity routeState = stateManager.getRouteState(shipment_route, StateFactory.LOAD_AT_BEGINNING, Capacity.class);
        assertEquals(0,routeState.get(0));
    }

    @Test
    public void when_shipmentroute_loadAtAct1ShouldBe10(){
        Capacity atAct1 = stateManager.getActivityState(shipment_route.getActivities().get(0), StateFactory.LOAD, Capacity.class);
        assertEquals(10,atAct1.get(0));
    }

    @Test
    public void when_shipmentroute_loadAtAct2ShouldBe15(){
        Capacity atAct2 = stateManager.getActivityState(shipment_route.getActivities().get(1), StateFactory.LOAD, Capacity.class);
        assertEquals(15,atAct2.get(0));
    }

    @Test
    public void when_shipmentroute_loadAtAct3ShouldBe10(){
        Capacity atAct = stateManager.getActivityState(shipment_route.getActivities().get(2), StateFactory.LOAD, Capacity.class);
        assertEquals(10, atAct.get(0));
    }

    @Test
    public void when_shipmentroute_loadAtAct4ShouldBe0(){
        Capacity atAct = stateManager.getActivityState(shipment_route.getActivities().get(3), StateFactory.LOAD, Capacity.class);
        assertEquals(0, atAct.get(0));
    }

    @Test
    public void when_shipmentroute_futureMaxLoatAtAct1ShouldBe15(){
        Capacity atAct1 = stateManager.getActivityState(shipment_route.getActivities().get(0), StateFactory.FUTURE_MAXLOAD, Capacity.class);
        assertEquals(15,atAct1.get(0));
    }

    @Test
    public void when_shipmentroute_futureMaxLoatAtAct2ShouldBe15(){
        Capacity atAct2 = stateManager.getActivityState(shipment_route.getActivities().get(1), StateFactory.FUTURE_MAXLOAD, Capacity.class);
        assertEquals(15,atAct2.get(0));
    }

    @Test
    public void when_shipmentroute_futureMaxLoatAtAct3ShouldBe10(){
        Capacity atAct = stateManager.getActivityState(shipment_route.getActivities().get(2), StateFactory.FUTURE_MAXLOAD, Capacity.class);
        assertEquals(10,atAct.get(0));
    }

    @Test
    public void when_shipmentroute_futureMaxLoatAtAct4ShouldBe0(){
        Capacity atAct = stateManager.getActivityState(shipment_route.getActivities().get(3), StateFactory.FUTURE_MAXLOAD, Capacity.class);
        assertEquals(0,atAct.get(0));
    }

    @Test
    public void when_shipmentroute_pastMaxLoatAtAct1ShouldBe10(){
        Capacity atAct1 = stateManager.getActivityState(shipment_route.getActivities().get(0), StateFactory.PAST_MAXLOAD, Capacity.class);
        assertEquals(10,atAct1.get(0));
    }

    @Test
    public void when_shipmentroute_pastMaxLoatAtAct2ShouldBe10(){
        Capacity atAct2 = stateManager.getActivityState(shipment_route.getActivities().get(1), StateFactory.PAST_MAXLOAD, Capacity.class);
        assertEquals(15,atAct2.get(0));
    }

    @Test
    public void when_shipmentroute_pastMaxLoatAtAct3ShouldBe15(){
        Capacity atAct = stateManager.getActivityState(shipment_route.getActivities().get(2), StateFactory.PAST_MAXLOAD, Capacity.class);
        assertEquals(15,atAct.get(0));
    }

    @Test
    public void when_shipmentroute_pastMaxLoatAtAct4ShouldBe15(){
        Capacity atAct = stateManager.getActivityState(shipment_route.getActivities().get(3), StateFactory.PAST_MAXLOAD, Capacity.class);
        assertEquals(15,atAct.get(0));
    }
}
