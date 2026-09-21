import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'dart:convert';
import 'bus_details_screen.dart';
import 'details_bus.dart';

class FavoriteBusesScreen extends StatefulWidget {
  const FavoriteBusesScreen({Key? key}) : super(key: key);

  @override
  State<FavoriteBusesScreen> createState() => _FavoriteBusesScreenState();
}

class _FavoriteBusesScreenState extends State<FavoriteBusesScreen> {
  List<Map<String, dynamic>> _favorites = [];
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    _loadFavorites();
  }

  Future<void> _loadFavorites() async {
    final prefs = await SharedPreferences.getInstance();
    final favs = prefs.getStringList('favorite_buses') ?? [];
    setState(() {
      _favorites = favs.map((b) => Map<String, dynamic>.from(json.decode(b))).toList();
      _isLoading = false;
    });
  }

  Future<void> _removeFavorite(Map<String, dynamic> bus) async {
    final prefs = await SharedPreferences.getInstance();
    final favs = prefs.getStringList('favorite_buses') ?? [];
    favs.removeWhere((b) {
      final map = json.decode(b);
      return (map['bus_name'] ?? map['english']) == (bus['bus_name'] ?? bus['english']);
    });
    await prefs.setStringList('favorite_buses', favs);
    _loadFavorites();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text("Favorite Buses"),
        centerTitle: true,
        backgroundColor: Colors.indigo,
      ),
      body: _isLoading
          ? Center(child: CircularProgressIndicator())
          : _favorites.isEmpty
              ? Center(child: Text("No favorite buses yet.", style: TextStyle(fontSize: 18)))
              : ListView.builder(
                  itemCount: _favorites.length,
                  itemBuilder: (context, index) {
                    final bus = _favorites[index];
                    return Card(
                      margin: EdgeInsets.symmetric(vertical: 10, horizontal: 16),
                      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(18)),
                      elevation: 6,
                      child: ListTile(
                        contentPadding: EdgeInsets.symmetric(vertical: 12, horizontal: 18),
                        leading: ClipRRect(
                          borderRadius: BorderRadius.circular(12),
                          child: bus['image'] != null && bus['image'].toString().isNotEmpty
                              ? Image.network(bus['image'], width: 56, height: 56, fit: BoxFit.cover)
                              : Container(
                                  width: 56,
                                  height: 56,
                                  color: Colors.indigo[50],
                                  child: Icon(Icons.directions_bus, color: Colors.indigo, size: 36),
                                ),
                        ),
                        title: Text(
                          bus['bus_name'] ?? bus['english'] ?? '',
                          style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold, color: Colors.indigo[900]),
                        ),
                        subtitle: Text(bus['service_type'] ?? '', style: TextStyle(color: Colors.indigo[700])),
                        trailing: IconButton(
                          icon: Icon(Icons.delete, color: Colors.red),
                          onPressed: () => _removeFavorite(bus),
                        ),
                        onTap: () {
                          if (bus.containsKey('bus_name')) {
                            Navigator.push(
                              context,
                              MaterialPageRoute(builder: (context) => BusDetailsScreen(bus: bus)),
                            );
                          } else {
                            Navigator.push(
                              context,
                              MaterialPageRoute(builder: (context) => DetailsBus(bus: bus)),
                            );
                          }
                        },
                      ),
                    );
                  },
                ),
    );
  }
}
