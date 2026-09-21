import 'package:flutter/material.dart';
import 'dart:convert';
import 'package:flutter/services.dart' show rootBundle;
import 'bus_search_screen.dart';
import 'bus_fare_search_screen.dart';
import 'details_bus.dart';
import 'favorite_buses_screen.dart';

class BusNameSearchScreen extends StatefulWidget {
  const BusNameSearchScreen({super.key});

  @override
  _BusNameSearchScreenState createState() => _BusNameSearchScreenState();
}

class _BusNameSearchScreenState extends State<BusNameSearchScreen> {
  final TextEditingController _controller = TextEditingController();
  List<Map<String, dynamic>> _allBuses = [];
  List<Map<String, dynamic>> _results = [];
  bool _isLoading = false;
  List<String> _allBusNames = [];
  String _message = "";

  @override
  void initState() {
    super.initState();
    _loadBusData();
  }

  Future<void> _loadBusData() async {
    setState(() {
      _isLoading = true;
      _message = "";
    });
    try {
      final String jsonString = await rootBundle.loadString('assets/dhaka-city-local-bus.json');
      final Map<String, dynamic> jsonData = json.decode(jsonString);
      final List<dynamic> buses = jsonData['data'] ?? [];
      _allBuses = buses.cast<Map<String, dynamic>>();
      _allBusNames = _allBuses.map((bus) => bus['english'].toString()).toList();
      setState(() {
        _results = List<Map<String, dynamic>>.from(_allBuses);
        _isLoading = false;
        _message = _results.isEmpty ? "🚫 No buses found." : "";
      });
    } catch (e) {
      print("❌ Error loading bus data: $e");
      setState(() {
        _isLoading = false;
        _message = "Failed to load bus data.";
      });
    }
  }

  void _searchBuses(String query) {
    setState(() {
      _isLoading = true;
      _message = "";
    });
    Future.delayed(Duration(milliseconds: 100), () {
      List<Map<String, dynamic>> results;
      if (query.isEmpty) {
        results = List<Map<String, dynamic>>.from(_allBuses);
      } else {
        results = _allBuses.where((bus) =>
          (bus['english'] ?? '').toString().toLowerCase().contains(query.toLowerCase())
        ).toList();
      }
      setState(() {
        _results = results;
        _isLoading = false;
        _message = results.isEmpty ? "🚫 No buses found for this name." : "";
      });
    });
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        flexibleSpace: Container(
          decoration: const BoxDecoration(
            gradient: LinearGradient(
              colors: [Color(0xFF6D9EFF), Color(0xFF4F8AFF)],
              begin: Alignment.topLeft,
              end: Alignment.bottomRight,
            ),
          ),
        ),
        title: Text("🚌 Search Buses by Name"),
        elevation: 0,
      ),
      drawer: Drawer(
        child: Container(
          decoration: const BoxDecoration(
            gradient: LinearGradient(
              colors: [Color(0xFF6D9EFF), Color(0xFF4F8AFF)],
              begin: Alignment.topLeft,
              end: Alignment.bottomRight,
            ),
          ),
          child: ListView(
            padding: EdgeInsets.zero,
            children: <Widget>[
              UserAccountsDrawerHeader(
                decoration: BoxDecoration(
                  color: Colors.transparent,
                ),
                accountName: Text('Welcome!', style: TextStyle(fontWeight: FontWeight.bold)),
                accountEmail: Text('Enjoy your ride'),
                currentAccountPicture: CircleAvatar(
                  backgroundColor: Colors.white,
                  child: Icon(Icons.directions_bus, color: Colors.indigo, size: 36),
                ),
              ),
              ListTile(
                leading: Icon(Icons.home, color: Colors.white),
                title: Text('Home', style: TextStyle(color: Colors.white)),
                onTap: () {
                  Navigator.pop(context);
                  Navigator.push(
                    context,
                    MaterialPageRoute(
                      builder: (context) => BusSearchScreen(),
                    ),
                  );
                },
              ),
              ListTile(
                leading: Icon(Icons.search, color: Colors.white),
                title: Text('Search Buses', style: TextStyle(color: Colors.white)),
                onTap: () {
                  Navigator.pop(context);
                },
              ),
              ListTile(
                leading: Icon(Icons.attach_money, color: Colors.white),
                title: Text('Bus Fare', style: TextStyle(color: Colors.white)),
                onTap: () {
                  Navigator.push(
                    context,
                    MaterialPageRoute(builder: (context) => BusFareSearchScreen()),
                  );
                },
              ),
              ListTile(
                leading: Icon(Icons.info, color: Colors.white),
                title: Text('Favorite', style: TextStyle(color: Colors.white)),
                onTap: () {
                  Navigator.push(
                    context,
                    MaterialPageRoute(builder: (context) => FavoriteBusesScreen()),
                  );
                },
              ),
            ],
          ),
        ),
      ),
      body: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 20.0, vertical: 32.0),
        child: Column(
          children: [
            Material(
              elevation: 0,
              borderRadius: BorderRadius.circular(16),
              child: Padding(
                padding: const EdgeInsets.symmetric(horizontal: 0.0),
                child: Stack(
                  children: [
                    Container(
                      decoration: BoxDecoration(
                        color: Colors.white,
                        borderRadius: BorderRadius.circular(16),
                        boxShadow: [
                          BoxShadow(
                            color: Colors.black12,
                            blurRadius: 12,
                            offset: Offset(0, 4),
                          ),
                        ],
                      ),
                      child: TextField(
                        controller: _controller,
                        decoration: InputDecoration(
                          hintText: 'Search bus by name...',
                          labelText: 'Bus Name',
                          labelStyle: TextStyle(color: Colors.indigo[700], fontWeight: FontWeight.w600),
                          filled: true,
                          fillColor: Colors.white,
                          border: OutlineInputBorder(
                            borderRadius: BorderRadius.circular(16),
                            borderSide: BorderSide.none,
                          ),
                          prefixIcon: Icon(Icons.search, color: Colors.indigo[400]),
                          contentPadding: EdgeInsets.symmetric(vertical: 20, horizontal: 18),
                        ),
                        style: TextStyle(fontSize: 18),
                        onChanged: (value) {
                          _searchBuses(value);
                          setState(() {});
                        },
                        onSubmitted: (value) => _searchBuses(value),
                      ),
                    ),
                    if (_controller.text.isNotEmpty &&
                        _allBusNames.any((name) => name.toLowerCase().contains(_controller.text.toLowerCase())))
                      Positioned(
                        left: 0,
                        right: 0,
                        top: 68,
                        child: Material(
                          elevation: 8,
                          borderRadius: BorderRadius.circular(12),
                          color: Colors.transparent,
                          child: Container(
                            decoration: BoxDecoration(
                              color: Colors.white,
                              borderRadius: BorderRadius.circular(12),
                              boxShadow: [
                                BoxShadow(
                                  color: Colors.black12,
                                  blurRadius: 8,
                                  offset: Offset(0, 2),
                                ),
                              ],
                            ),
                            child: ListView(
                              shrinkWrap: true,
                              padding: EdgeInsets.zero,
                              children: _allBusNames
                                  .where((name) => name.toLowerCase().contains(_controller.text.toLowerCase()))
                                  .take(5)
                                  .map((suggestion) => ListTile(
                                        title: Text(suggestion, style: TextStyle(fontWeight: FontWeight.w500)),
                                        hoverColor: Colors.indigo[50],
                                        onTap: () {
                                          _controller.text = suggestion;
                                          _searchBuses(suggestion);
                                          FocusScope.of(context).unfocus();
                                          setState(() {});
                                        },
                                      ))
                                  .toList(),
                            ),
                          ),
                        ),
                      ),
                  ],
                ),
              ),
            ),
            SizedBox(height: 16),
            ElevatedButton.icon(
              onPressed: () {
                _searchBuses(_controller.text);
              },
              icon: Icon(Icons.search),
              label: Text('Find Bus'),
              style: ElevatedButton.styleFrom(
                padding: EdgeInsets.symmetric(horizontal: 32, vertical: 16),
                textStyle: TextStyle(fontSize: 20),
                elevation: 4,
              ),
            ),
            SizedBox(height: 28),
            _isLoading
                ? Center(child: CircularProgressIndicator())
                : _message.isNotEmpty
                    ? Padding(
                        padding: const EdgeInsets.all(8.0),
                        child: Text(_message, style: TextStyle(color: Colors.red, fontSize: 18)),
                      )
                    : Expanded(
                        child: ListView.builder(
                          itemCount: _results.length,
                          itemBuilder: (context, index) {
                            var bus = _results[index];
                            return Card(
                              margin: EdgeInsets.symmetric(vertical: 10, horizontal: 2),
                              shape: RoundedRectangleBorder(
                                borderRadius: BorderRadius.circular(18),
                              ),
                              elevation: 6,
                              child: ListTile(
                                contentPadding: EdgeInsets.symmetric(vertical: 12, horizontal: 18),
                                leading: ClipRRect(
                                  borderRadius: BorderRadius.circular(12),
                                  child: bus['image'] != null &&
                                          bus['image'] is String &&
                                          bus['image'].isNotEmpty
                                      ? Image.network(
                                          bus['image'],
                                          width: 56,
                                          height: 56,
                                          fit: BoxFit.cover,
                                        )
                                      : Container(
                                          width: 56,
                                          height: 56,
                                          color: Colors.indigo[50],
                                          child: Icon(Icons.directions_bus, color: Colors.indigo, size: 36),
                                        ),
                                ),
                                title: Text(
                                  bus['english'] ?? '',
                                  style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold, color: Colors.indigo[900]),
                                ),
                                subtitle: Text(bus['service_type'] ?? '', style: TextStyle(color: Colors.indigo[700])),
                                trailing: Icon(Icons.arrow_forward_ios, color: Colors.indigo[400]),
                                onTap: () {
                                  Navigator.push(
                                    context,
                                    MaterialPageRoute(
                                      builder: (context) => DetailsBus(bus: bus),
                                    ),
                                  );
                                },
                              ),
                            );
                          },
                        ),
                      ),
          ],
        ),
      ),
    );
  }
}
