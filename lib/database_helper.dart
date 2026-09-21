import 'dart:convert';
import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:sqflite/sqflite.dart';
import 'package:path/path.dart';

class DatabaseHelper {
  static final DatabaseHelper _instance = DatabaseHelper._internal();
  static Database? _database;
  static List<Map<String, dynamic>>? _cachedBuses;

  DatabaseHelper._internal();

  factory DatabaseHelper() {
    return _instance;
  }

  Future<List<Map<String, dynamic>>> _loadBusesFromJson() async {
    if (_cachedBuses != null) return _cachedBuses!;
    try {
      final jsonString = await rootBundle.loadString('assets/dhaka-city-local-bus.json');
      final Map<String, dynamic> data = json.decode(jsonString);
      final List<dynamic> list = data['data'] ?? [];
      _cachedBuses = list.map<Map<String, dynamic>>((b) {
        final routes = List<String>.from(b['routes'] ?? []);
        return {
          'bus_name': b['english'] ?? '',
          'service_type': b['service_type'] ?? '',
          'image': b['image'] ?? '',
          'stops': routes.join(','),
        };
      }).toList();
      return _cachedBuses!;
    } catch (e) {
      print("Error loading json fallback: $e");
      return [];
    }
  }

  Future<Database?> get database async {
    if (kIsWeb) return null;
    if (_database != null) return _database!;
    try {
      _database = await _initDatabase();
      return _database;
    } catch (e) {
      print("SQLite init failed: $e");
      return null;
    }
  }

  Future<Database> _initDatabase() async {
    String path = join(await getDatabasesPath(), 'busease.db');
    return await openDatabase(
      path,
      version: 1,
      onCreate: (db, version) async {
        await db.execute('''
          CREATE TABLE IF NOT EXISTS bus_routes (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            bus_name TEXT,
            service_type TEXT,
            image TEXT,
            stops TEXT
          )
        ''');
      },
    );
  }

  Future<void> insertBusRoute(String name, String serviceType, String image, List<String> stops) async {
    final db = await database;
    if (db == null) return;
    await db.insert(
      'bus_routes',
      {
        'bus_name': name,
        'service_type': serviceType,
        'image': image,
        'stops': stops.join(','),
      },
      conflictAlgorithm: ConflictAlgorithm.replace,
    );
  }

  Future<List<Map<String, dynamic>>> getAllRoutes() async {
    try {
      final db = await database;
      if (db != null) {
        final res = await db.query('bus_routes');
        if (res.isNotEmpty) return res;
      }
    } catch (e) {
      print("Error querying db: $e");
    }
    return await _loadBusesFromJson();
  }

  Future<List<Map<String, dynamic>>> searchBusRoutes(String start, String end) async {
    try {
      final db = await database;
      if (db != null) {
        List<Map<String, dynamic>> results = await db.query(
          'bus_routes',
          where: "stops LIKE ? AND stops LIKE ?",
          whereArgs: ['%$start%', '%$end%'],
        );
        if (results.isNotEmpty) return results;
      }
    } catch (e) {
      print("SQLite Query Error: $e");
    }

    final all = await _loadBusesFromJson();
    final lowerStart = start.trim().toLowerCase();
    final lowerEnd = end.trim().toLowerCase();
    return all.where((b) {
      final stops = (b['stops'] as String? ?? '').toLowerCase();
      return stops.contains(lowerStart) && stops.contains(lowerEnd);
    }).toList();
  }

  Future<List<Map<String, dynamic>>> searchBusesByName(String query) async {
    try {
      final db = await database;
      if (db != null) {
        final results = await db.query(
          'bus_routes',
          where: 'bus_name LIKE ? OR service_type LIKE ?',
          whereArgs: ['%$query%', '%$query%'],
        );
        if (results.isNotEmpty) return results;
      }
    } catch (e) {
      print("SQLite Query Error: $e");
    }

    final all = await _loadBusesFromJson();
    final lowerQuery = query.trim().toLowerCase();
    return all.where((b) {
      final name = (b['bus_name'] as String? ?? '').toLowerCase();
      final type = (b['service_type'] as String? ?? '').toLowerCase();
      return name.contains(lowerQuery) || type.contains(lowerQuery);
    }).toList();
  }
}
