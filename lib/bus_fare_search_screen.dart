import 'dart:convert';
import 'bus_search_screen.dart';
import 'bus_name_search_screen.dart';
import 'favorite_buses_screen.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class BusFareSearchScreen extends StatefulWidget {
  const BusFareSearchScreen({super.key});

  @override
  _BusFareSearchScreenState createState() => _BusFareSearchScreenState();
}

class _BusFareSearchScreenState extends State<BusFareSearchScreen> {
  final TextEditingController _startController = TextEditingController();
  final TextEditingController _endController = TextEditingController();
  List<String> _allFromStops = [];
  Map<String, Set<String>> _fromToMap = {};
  String _message = "";
  List<Map<String, dynamic>> _searchResults = [];
  bool _isLoadingStops = true;

  @override
  void initState() {
    super.initState();
    _loadAllStops();
  }

  Future<void> _loadAllStops() async {
    try {
      final String jsonString = await rootBundle.loadString('assets/kotovara_full_data.json');
      final List<dynamic> data = json.decode(jsonString);

      Set<String> uniqueFrom = {};
      Map<String, Set<String>> fromToMap = {};

      for (var route in data) {
        final from = route['From'];
        final to = route['To'];
        if (from != null) {
          uniqueFrom.add(from);
          fromToMap.putIfAbsent(from, () => <String>{});
          if (to != null) {
            fromToMap[from]!.add(to);
          }
        }
      }

      setState(() {
        _allFromStops = uniqueFrom.toList()..sort();
        _fromToMap = fromToMap;
        _isLoadingStops = false;
      });
    } catch (e) {
      setState(() {
        _isLoadingStops = false;
      });
    }
  }

  Future<void> _searchFare() async {
    if (_startController.text.isEmpty || _endController.text.isEmpty) {
      setState(() {
        _message = "⚠️ Please enter both start and end locations.";
        _searchResults = [];
      });
      return;
    }

    try {
      final String jsonString = await rootBundle.loadString('assets/kotovara_full_data.json');
      final List<dynamic> data = json.decode(jsonString);

      final results = data.where((entry) {
        return entry['From'] == _startController.text &&
            entry['To'] == _endController.text;
      }).toList();

      setState(() {
        _searchResults = List<Map<String, dynamic>>.from(results);
        _message = results.isEmpty ? "🚫 No fare information found for the selected route." : "";
      });
    } catch (e) {
      setState(() {
        _message = "❌ Error loading fare data.";
        _searchResults = [];
      });
    }
  }

  Future<void> _showLocationDialog({
    required String title,
    required List<String> options,
    required TextEditingController controller,
    required VoidCallback? onSelected,
  }) async {
    String searchText = '';
    List<String> filteredOptions = List.from(options);

    await showDialog(
      context: context,
      builder: (context) {
        return StatefulBuilder(
          builder: (context, setState) {
            return Dialog(
              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
              child: Container(
                width: double.infinity,
                padding: EdgeInsets.all(16),
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
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Text(
                      "Select or Search Location",
                      style: TextStyle(fontWeight: FontWeight.bold, fontSize: 18, color: Colors.blueGrey[900]),
                    ),
                    SizedBox(height: 8),
                    Container(
                      decoration: BoxDecoration(
                        color: Colors.grey[100],
                        borderRadius: BorderRadius.circular(12),
                      ),
                      child: Row(
                        children: [
                          Icon(Icons.search, size: 28, color: Colors.indigo[400]),
                          SizedBox(width: 8),
                          Expanded(
                            child: TextField(
                              autofocus: true,
                              decoration: InputDecoration(
                                hintText: 'Type to search...',
                                border: InputBorder.none,
                              ),
                              onChanged: (value) {
                                setState(() {
                                  searchText = value;
                                  filteredOptions = options
                                      .where((stop) => stop.toLowerCase().contains(searchText.toLowerCase()))
                                      .toList();
                                });
                              },
                            ),
                          ),
                        ],
                      ),
                    ),
                    Divider(),
                    Expanded(
                      child: ListView.separated(
                        shrinkWrap: true,
                        itemCount: filteredOptions.length,
                        separatorBuilder: (_, __) => Divider(height: 1),
                        itemBuilder: (context, index) {
                          final stop = filteredOptions[index];
                          return ListTile(
                            title: Text(stop, style: TextStyle(fontWeight: FontWeight.w500)),
                            hoverColor: Colors.indigo[50],
                            onTap: () {
                              controller.text = stop;
                              Navigator.of(context).pop();
                              if (onSelected != null) onSelected();
                            },
                          );
                        },
                      ),
                    ),
                    TextButton(
                      onPressed: () => Navigator.of(context).pop(),
                      child: Text("Close"),
                    ),
                  ],
                ),
              ),
            );
          },
        );
      },
    );
  }

  Widget _buildPopupLocationField({
    required String label,
    required TextEditingController controller,
    required List<String> options,
    required VoidCallback? onTap,
    required VoidCallback? onSelected,
  }) {
    return GestureDetector(
      onTap: () async {
        if (onTap != null) onTap();
        if (label.contains("End") && _startController.text.isEmpty) {
          setState(() {
            _message = "⚠️ Please select your start location first.";
          });
          return;
        }
        await _showLocationDialog(
          title: label,
          options: options,
          controller: controller,
          onSelected: onSelected,
        );
        if (label.contains("Start")) {
          _endController.clear();
          setState(() {});
        } else {
          setState(() {});
        }
      },
      child: AbsorbPointer(
        child: Container(
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
            controller: controller,
            decoration: InputDecoration(
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
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    List<String> endOptions = [];
    if (_fromToMap.containsKey(_startController.text)) {
      endOptions = _fromToMap[_startController.text]!.toList()..sort();
    }

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
        title: Text("Bus Fare Search"),
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
                  Navigator.pushReplacement(
                    context,
                    MaterialPageRoute(builder: (context) => BusSearchScreen()),
                  );
                },
              ),
              ListTile(
                leading: Icon(Icons.search, color: Colors.white),
                title: Text('Search Buses', style: TextStyle(color: Colors.white)),
                onTap: () {
                  Navigator.pushReplacement(
                    context,
                    MaterialPageRoute(builder: (context) => BusNameSearchScreen()),
                  );
                },
              ),
              ListTile(
                leading: Icon(Icons.attach_money, color: Colors.white),
                title: Text('Bus Fare', style: TextStyle(color: Colors.white)),
                onTap: () {
                  Navigator.pushReplacement(
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
        child: _isLoadingStops
            ? Center(child: CircularProgressIndicator())
            : SingleChildScrollView(
                child: Column(
                  children: [
                    _buildPopupLocationField(
                      label: "📍 Start Location",
                      controller: _startController,
                      options: _allFromStops,
                      onTap: null,
                      onSelected: () {
                        _endController.clear();
                        setState(() {});
                      },
                    ),
                    SizedBox(height: 16),
                    AbsorbPointer(
                      absorbing: _startController.text.isEmpty,
                      child: Opacity(
                        opacity: _startController.text.isEmpty ? 0.5 : 1.0,
                        child: _buildPopupLocationField(
                          label: "🏁 End Location",
                          controller: _endController,
                          options: endOptions,
                          onTap: () {
                            if (_startController.text.isEmpty) {
                              setState(() {
                                _message = "⚠️ Please select your start location first.";
                              });
                            }
                          },
                          onSelected: () {
                            setState(() {});
                          },
                        ),
                      ),
                    ),
                    SizedBox(height: 28),
                    ElevatedButton.icon(
                      onPressed: (_startController.text.isEmpty || _endController.text.isEmpty)
                          ? null
                          : _searchFare,
                      icon: Icon(Icons.search),
                      label: Text("Search Fare"),
                      style: ElevatedButton.styleFrom(
                        padding: EdgeInsets.symmetric(horizontal: 32, vertical: 16),
                        textStyle: TextStyle(fontSize: 20),
                        elevation: 4,
                      ),
                    ),
                    SizedBox(height: 28),
                    _message.isNotEmpty
                        ? Padding(
                            padding: const EdgeInsets.all(8.0),
                            child: Text(_message, style: TextStyle(color: Colors.red, fontSize: 18)),
                          )
                        : _searchResults.isEmpty
                            ? Container()
                            : ListView.builder(
                                shrinkWrap: true,
                                physics: NeverScrollableScrollPhysics(),
                                itemCount: _searchResults.length,
                                itemBuilder: (context, index) {
                                  final result = _searchResults[index];
                                  return Container(
                                    margin: EdgeInsets.symmetric(vertical: 10, horizontal: 2),
                                    decoration: BoxDecoration(
                                      gradient: LinearGradient(
                                        colors: [Color(0xFF6D9EFF), Color(0xFFB2CFFF)],
                                        begin: Alignment.topLeft,
                                        end: Alignment.bottomRight,
                                      ),
                                      borderRadius: BorderRadius.circular(22),
                                      boxShadow: [
                                        BoxShadow(
                                          color: Colors.blue.withOpacity(0.18),
                                          blurRadius: 18,
                                          offset: Offset(0, 8),
                                        ),
                                      ],
                                    ),
                                    child: Padding(
                                      padding: const EdgeInsets.symmetric(vertical: 18, horizontal: 22),
                                      child: Row(
                                        crossAxisAlignment: CrossAxisAlignment.start,
                                        children: [
                                          CircleAvatar(
                                            radius: 28,
                                            backgroundColor: Colors.white,
                                            child: Icon(Icons.attach_money, color: Colors.indigo[700], size: 32),
                                          ),
                                          SizedBox(width: 16),
                                          Expanded(
                                            child: Column(
                                              crossAxisAlignment: CrossAxisAlignment.start,
                                              children: [
                                                // Show start and end address in full, each on its own line
                                                Text(
                                                  "${result['From']}",
                                                  style: TextStyle(
                                                    fontSize: 20,
                                                    fontWeight: FontWeight.bold,
                                                    color: Colors.indigo[900],
                                                    letterSpacing: 0.5,
                                                  ),
                                                  softWrap: true,
                                                  overflow: TextOverflow.visible,
                                                ),
                                                Row(
                                                  children: [
                                                    Expanded(
                                                      child: Divider(
                                                        color: Colors.indigo[300],
                                                        thickness: 2,
                                                        height: 24,
                                                      ),
                                                    ),
                                                    Padding(
                                                      padding: const EdgeInsets.symmetric(horizontal: 8.0),
                                                      child: Icon(Icons.arrow_downward, color: Colors.indigo[700], size: 26),
                                                    ),
                                                    Expanded(
                                                      child: Divider(
                                                        color: Colors.indigo[300],
                                                        thickness: 2,
                                                        height: 24,
                                                      ),
                                                    ),
                                                  ],
                                                ),
                                                Text(
                                                  "${result['To']}",
                                                  style: TextStyle(
                                                    fontSize: 20,
                                                    fontWeight: FontWeight.bold,
                                                    color: Colors.indigo[900],
                                                    letterSpacing: 0.5,
                                                  ),
                                                  softWrap: true,
                                                  overflow: TextOverflow.visible,
                                                ),
                                                SizedBox(height: 12),
                                                Wrap(
                                                  spacing: 10,
                                                  runSpacing: 8,
                                                  children: [
                                                    Chip(
                                                      avatar: Icon(Icons.alt_route, color: Colors.white, size: 18),
                                                      label: Text(
                                                        "Route: ${result['Route']}",
                                                        style: TextStyle(color: Colors.white, fontWeight: FontWeight.w600),
                                                        overflow: TextOverflow.ellipsis,
                                                        maxLines: 2,
                                                      ),
                                                      backgroundColor: Colors.indigo[400],
                                                      shape: RoundedRectangleBorder(
                                                        borderRadius: BorderRadius.circular(12),
                                                      ),
                                                      materialTapTargetSize: MaterialTapTargetSize.shrinkWrap,
                                                    ),
                                                    Chip(
                                                      avatar: Icon(Icons.attach_money, color: Colors.white, size: 18),
                                                      label: Text(
                                                        "Fare: ${result['Fare']}",
                                                        style: TextStyle(color: Colors.white, fontWeight: FontWeight.w600),
                                                        overflow: TextOverflow.ellipsis,
                                                      ),
                                                      backgroundColor: Colors.green[400],
                                                      shape: RoundedRectangleBorder(
                                                        borderRadius: BorderRadius.circular(12),
                                                      ),
                                                      materialTapTargetSize: MaterialTapTargetSize.shrinkWrap,
                                                    ),
                                                    Chip(
                                                      avatar: Icon(Icons.social_distance, color: Colors.white, size: 18),
                                                      label: Text(
                                                        "Distance: ${result['Distance']}",
                                                        style: TextStyle(color: Colors.white, fontWeight: FontWeight.w600),
                                                        overflow: TextOverflow.ellipsis,
                                                      ),
                                                      backgroundColor: Colors.deepPurple[400],
                                                      shape: RoundedRectangleBorder(
                                                        borderRadius: BorderRadius.circular(12),
                                                      ),
                                                      materialTapTargetSize: MaterialTapTargetSize.shrinkWrap,
                                                    ),
                                                  ],
                                                ),
                                              ],
                                            ),
                                          ),
                                        ],
                                      ),
                                    ),
                                  );
                                },
                              ),
                  ],
                ),
              ),
      ),
    );
  }
}