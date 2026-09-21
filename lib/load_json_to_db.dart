import 'dart:convert';
import 'package:flutter/services.dart';
import 'package:sqflite/sqflite.dart';
import 'database_helper.dart';

Future<void> insertBusDataFromJson() async {
  try {
    String jsonString = await rootBundle.loadString('assets/dhaka-city-local-bus.json');
    Map<String, dynamic> jsonData = jsonDecode(jsonString);
    List<dynamic> busList = jsonData["data"];

    final db = await DatabaseHelper().database;
    Batch batch = db.batch();

    for (var bus in busList) {
      List<String> routes = List<String>.from(bus["routes"] ?? []);
      
      // Delete existing bus entry if it exists
      await db.delete(
        'bus_routes',
        where: 'bus_name = ?',
        whereArgs: [bus["english"]],
      );

      // Insert new bus entry
      batch.insert(
        'bus_routes',
        {
          'bus_name': bus["english"],
          'service_type': bus["service_type"] ?? "",
          'image': bus["image"] ?? "",
          'stops': routes.join(','),
        },
        conflictAlgorithm: ConflictAlgorithm.replace,
      );
    }

    await batch.commit();
    print("🚍 All Bus Routes Inserted into SQLite");
  } catch (e) {
    print("❌ Error inserting bus data: $e");
  }
}