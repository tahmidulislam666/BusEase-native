import 'package:flutter/material.dart';
import 'database_helper.dart';
import 'bus_details_screen.dart';
import 'bus_name_search_screen.dart';
import 'bus_fare_search_screen.dart'; // Import the new screen
import 'favorite_buses_screen.dart';

class BusSearchScreen extends StatefulWidget {
  const BusSearchScreen({super.key});

  @override
  _BusSearchScreenState createState() => _BusSearchScreenState();
}

class _BusSearchScreenState extends State<BusSearchScreen> {
  final TextEditingController _startController = TextEditingController();
  final TextEditingController _endController = TextEditingController();
  List<String> _allStops = [];
  List<Map<String, dynamic>> _busResults = [];
  bool _isLoading = false;
  String _message = "";
  bool _isLoadingStops = true; // Add this

  @override
  void initState() {
    super.initState();
    _loadAllStops();
  }

  Future<void> _loadAllStops() async {
    try {
      final List<Map<String, dynamic>> routes = await DatabaseHelper().getAllRoutes();

      Set<String> uniqueStops = {};
      for (var route in routes) {
        if (route['stops'] != null) {
          List<String> stops = route['stops'].toString().split(',');
          for (var s in stops) {
            final trimmed = s.trim();
            if (trimmed.isNotEmpty) {
              uniqueStops.add(trimmed);
            }
          }
        }
      }

      setState(() {
        _allStops = uniqueStops.toList()..sort();
        _isLoadingStops = false;
      });
    } catch (e) {
      print("❌ Error loading stops: $e");
      setState(() {
        _isLoadingStops = false;
      });
    }
  }

  Future<void> searchBuses() async {
    if (_startController.text.isEmpty || _endController.text.isEmpty) {
      setState(() {
        _message = "⚠️ Please enter both start and end locations.";
        _busResults = [];
      });
      return;
    }

    setState(() {
      _isLoading = true;
      _busResults = [];
      _message = "";
    });

    try {
      DatabaseHelper dbHelper = DatabaseHelper();
      List<Map<String, dynamic>> results = await dbHelper.searchBusRoutes(
        _startController.text,
        _endController.text,
      );

      setState(() {
        _busResults = List<Map<String, dynamic>>.from(results);
        _isLoading = false;
        _message = results.isEmpty ? "🚫 No buses found for the selected route." : "";
      });
    } catch (e) {
      setState(() {
        _isLoading = false;
        _message = "❌ Error searching for buses.";
      });
      print("❌ Error searching buses: $e");
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      extendBodyBehindAppBar: true,
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
        title: Text("🚌 Bus Search"),
        leading: Builder(
          builder: (context) => IconButton(
            icon: Icon(Icons.menu),
            onPressed: () => Scaffold.of(context).openDrawer(),
          ),
        ),
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
                },
              ),
              ListTile(
                leading: Icon(Icons.search, color: Colors.white),
                title: Text('Search Buses', style: TextStyle(color: Colors.white)),
                onTap: () {
                  Navigator.push(
                    context,
                    MaterialPageRoute(builder: (context) => BusNameSearchScreen()),
                  );
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
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 20.0, vertical: 32.0),
          child: _isLoadingStops
              ? Center(child: CircularProgressIndicator())
              : Column(
                  children: [
                    // Always show both fields
                    _buildAutoCompleteField("📍 Start Location", _startController),
                    SizedBox(height: 16),
                    _buildAutoCompleteField("🏁 End Location", _endController),
                    SizedBox(height: 28),
                    ElevatedButton.icon(
                      onPressed: searchBuses,
                      icon: Icon(Icons.search),
                      label: Text("Find Bus"),
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
                                  itemCount: _busResults.length,
                                  itemBuilder: (context, index) {
                                    var bus = _busResults[index];
                                    List<String> stops = bus['stops'].split(',');

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
                                          child: bus['image'] != null && bus['image'].isNotEmpty
                                              ? Image.network(bus['image'], width: 56, height: 56, fit: BoxFit.cover)
                                              : Container(
                                                  width: 56,
                                                  height: 56,
                                                  color: Colors.indigo[50],
                                                  child: Icon(Icons.directions_bus, color: Colors.indigo, size: 36),
                                                ),
                                        ),
                                        title: Text(bus['bus_name'],
                                            style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold, color: Colors.indigo[900])),
                                        subtitle: Column(
                                          crossAxisAlignment: CrossAxisAlignment.start,
                                          children: [
                                            SizedBox(height: 6),
                                            Row(
                                              children: [
                                                Text(stops.first, style: TextStyle(fontSize: 16, color: Colors.indigo[700])),
                                                SizedBox(width: 5),
                                                Icon(Icons.swap_horiz, size: 18, color: Colors.indigo[400]),
                                                SizedBox(width: 5),
                                                Text(stops.last, style: TextStyle(fontSize: 16, color: Colors.indigo[700])),
                                              ],
                                            ),
                                          ],
                                        ),
                                        trailing: Icon(Icons.arrow_forward_ios, color: Colors.indigo[400]),
                                        onTap: () {
                                          Navigator.push(
                                            context,
                                            MaterialPageRoute(
                                              builder: (context) => BusDetailsScreen(bus: bus),
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
      ),
    );
  }

  /// 🔍 Creates an Autocomplete text field for location search
  Widget _buildAutoCompleteField(String label, TextEditingController controller) {
    return Autocomplete<String>(
      initialValue: TextEditingValue(text: controller.text),
      optionsBuilder: (TextEditingValue textEditingValue) {
        if (textEditingValue.text.isEmpty) {
          return _allStops;
        }
        return _allStops.where((stop) => stop.toLowerCase().contains(textEditingValue.text.toLowerCase()));
      },
      onSelected: (String selection) {
        controller.text = selection;
      },
      fieldViewBuilder: (context, textEditingController, focusNode, onFieldSubmitted) {
        return Container(
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
            controller: textEditingController,
            focusNode: focusNode,
            onChanged: (val) {
              controller.text = val;
            },
            decoration: InputDecoration(
              hintText: 'Type to search...',
              labelText: label,
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
          ),
        );
      },
    );
  }
}