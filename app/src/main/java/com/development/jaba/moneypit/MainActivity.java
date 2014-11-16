package com.development.jaba.moneypit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.development.jaba.database.Car;
import com.development.jaba.database.MoneyPitDbContext;

import java.util.List;

public class MainActivity extends Activity {

    private static final int REQUEST_EDIT_CAR = 1,
                             REQUEST_ADD_CAR = 2;

    private MoneyPitDbContext context;
    private ListAdapter carAdapter;
    private ListView carList;
    private List<Car> cars;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = new MoneyPitDbContext(this);

/*        Car car = context.getCarByLicensePlate("98-TX-NV");
        if(car == null) {
            car = new Car();
            car.setVolumeUnit(VolumeUnit.Liter);
            car.setDistanceUnit(DistanceUnit.Kilometer);
            car.setMake("Peugeot");
            car.setModel("207");
            car.setLicensePlate("98-TX-NV");
            car.setBuildYear(2007);
            car.setCurrency("EUR");
            long id = (int)c.addCar(car);

            Fillup f = new Fillup();
            f.setCarId((int)id);
            f.setDate(Utils.getDateFromDateTime("2012-12-31 12:00:00"));
            f.setFullTank(true);
            f.setOdometer(100000);
            f.setPrice(1.589);
            f.setVolume(33.51);

            long fid = c.addFillup(f);

            f = new Fillup();
            f.setCarId((int)id);
            f.setDate(Utils.getDateFromDateTime("2013-01-07 12:00:00"));
            f.setFullTank(true);
            f.setOdometer(100500);
            f.setPrice(1.589);
            f.setVolume(32.31);

            c.addFillup(f);

            f = new Fillup();
            f.setCarId((int)id);
            f.setDate(Utils.getDateFromDateTime("2013-01-13 12:00:00"));
            f.setFullTank(true);
            f.setOdometer(101000);
            f.setPrice(1.589);
            f.setVolume(29.71);

            c.addFillup(f);


        }

        List<Fillup> fu = c.getFillupsOfCar(car.getId(),2013);

        for(Fillup fup : fu)
        {
            Toast.makeText(this, String.valueOf(fup.getDaysSinceLastFillup()), Toast.LENGTH_SHORT).show();
        }*/

        cars = context.getAllCars();

/*        for (Car cr : cars) {
            Toast t = Toast.makeText(this, cr.toString(), Toast.LENGTH_SHORT);
            t.show();
        }*/

        carAdapter = new CarRowAdapter(this, cars);
        carList = (ListView) findViewById(R.id.carList);
        carList.setEmptyView(findViewById(R.id.listEmpty));

        carList.setAdapter(carAdapter);
        carList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                editCar((Car)carAdapter.getItem(position));
            }
        });

        registerForContextMenu(carList);
    }

    private void editCar(Car car) {
        Intent editCar = new Intent(MainActivity.this, AddOrEditCarActivity.class);
        if(car != null) {
            editCar.putExtra("Car", car);
        }
        startActivityForResult(editCar, car == null ? REQUEST_ADD_CAR : REQUEST_EDIT_CAR);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && data != null) {
            if (data.getExtras().containsKey("Car")) {
                Car car = (Car) data.getExtras().get("Car");
                if (car != null) {
                    if (requestCode == REQUEST_EDIT_CAR) {
                        for (int i = 0; i < cars.size(); i++) {
                            if (cars.get(i).getId() == car.getId()) {
                                cars.set(i, car);
                                break;
                            }
                        }
                    } else {
                        cars.add(car);
                    }
                    carList.invalidateViews();
                }
            }
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if(v.getId() == R.id.carList) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
            Car selectedCar = (Car)carAdapter.getItem(info.position);
            menu.setHeaderTitle(selectedCar.toString());
            String[] menuItems = getResources().getStringArray(R.array.edit_delete);
            for(int i = 0; i < menuItems.length; i++) {
                menu.add(Menu.NONE, i, i, menuItems[i]);
            }

        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int menuItemIndex = item.getItemId();
        switch(menuItemIndex) {
            case 0:
            {
                Car selectedCar = (Car)carAdapter.getItem(info.position);
                editCar(selectedCar);
                return true;
            }

            case 1:
                Car selectedCar = (Car)carAdapter.getItem(info.position);
                context.deleteCar(selectedCar);
                cars.remove(selectedCar);
                carList.invalidateViews();
                return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        else if (id == R.id.action_add_car) {
            editCar(null);
        }
        else if (id == R.id.show_database){
            Intent dbmanager = new Intent(MainActivity.this,AndroidDatabaseManager.class);
            startActivity(dbmanager);
        }

        return super.onOptionsItemSelected(item);
    }
}
