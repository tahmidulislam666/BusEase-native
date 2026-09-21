import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'dart:convert';
import 'dart:ui';

class DetailsBus extends StatefulWidget {
  final Map<String, dynamic> bus;

  const DetailsBus({Key? key, required this.bus}) : super(key: key);

  @override
  State<DetailsBus> createState() => _DetailsBusState();
}

class _DetailsBusState extends State<DetailsBus> {
  bool _isFavorite = false;

  @override
  void initState() {
    super.initState();
    _checkFavorite();
  }

  Future<void> _checkFavorite() async {
    final prefs = await SharedPreferences.getInstance();
    final favs = prefs.getStringList('favorite_buses') ?? [];
    setState(() {
      _isFavorite = favs.any((b) {
        final map = json.decode(b);
        return map['english'] == widget.bus['english'];
      });
    });
  }

  Future<void> _toggleFavorite() async {
    final prefs = await SharedPreferences.getInstance();
    final favs = prefs.getStringList('favorite_buses') ?? [];
    final busJson = json.encode(widget.bus);
    bool exists = favs.any((b) {
      final map = json.decode(b);
      return map['english'] == widget.bus['english'];
    });
    if (exists) {
      favs.removeWhere((b) {
        final map = json.decode(b);
        return map['english'] == widget.bus['english'];
      });
    } else {
      favs.add(busJson);
    }
    await prefs.setStringList('favorite_buses', favs);
    setState(() {
      _isFavorite = !exists;
    });
  }

  @override
  Widget build(BuildContext context) {
    List<String> stops = [];
    if (widget.bus['stops'] != null && widget.bus['stops'] is String) {
      stops = List<String>.from((widget.bus['stops'] as String).split(',').map((s) => s.trim()));
    } else if (widget.bus['routes'] != null && widget.bus['routes'] is List) {
      stops = List<String>.from(widget.bus['routes'].map((s) => s.toString()));
    }

    return Scaffold(
      extendBodyBehindAppBar: true,
      backgroundColor: Colors.transparent,
      appBar: AppBar(
        backgroundColor: Colors.transparent,
        elevation: 0,
        iconTheme: const IconThemeData(color: Colors.white),
        title: Text(
          widget.bus['bus_name'] ?? widget.bus['english'] ?? 'Bus Details',
          style: const TextStyle(
            color: Colors.white,
            fontWeight: FontWeight.bold,
            fontSize: 26,
            letterSpacing: 1.2,
          ),
        ),
        centerTitle: true,
      ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: _toggleFavorite,
        backgroundColor: _isFavorite ? Colors.yellow[700] : Colors.white.withOpacity(0.85),
        elevation: 8,
        icon: Icon(
          _isFavorite ? Icons.star : Icons.star_border,
          color: Color(0xFF4F8AFF),
        ),
        label: Text(
          _isFavorite ? "Favorited" : "Favorite",
          style: TextStyle(
            color: Color(0xFF4F8AFF),
            fontWeight: FontWeight.bold,
            fontSize: 18,
          ),
        ),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
      ),
      body: Stack(
        children: [
          // Gradient background
          Container(
            decoration: const BoxDecoration(
              gradient: LinearGradient(
                colors: [Color(0xFF4F8AFF), Color(0xFF6D9EFF), Color(0xFFB2CFFF)],
                begin: Alignment.topCenter,
                end: Alignment.bottomCenter,
              ),
            ),
          ),
          // Main content
          SingleChildScrollView(
            child: Padding(
              padding: const EdgeInsets.only(top: 90.0, left: 18, right: 18, bottom: 24),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.center,
                children: [
                  // Glassmorphism Card
                  ClipRRect(
                    borderRadius: BorderRadius.circular(32),
                    child: BackdropFilter(
                      filter: ImageFilter.blur(sigmaX: 18, sigmaY: 18),
                      child: Container(
                        width: double.infinity,
                        decoration: BoxDecoration(
                          color: Colors.white.withOpacity(0.18),
                          borderRadius: BorderRadius.circular(32),
                          border: Border.all(color: Colors.white.withOpacity(0.25), width: 1.5),
                        ),
                        child: Padding(
                          padding: const EdgeInsets.all(24.0),
                          child: Column(
                            children: [
                              Hero(
                                tag: widget.bus['image'] ?? widget.bus['bus_name'] ?? '',
                                child: ClipRRect(
                                  borderRadius: BorderRadius.circular(22),
                                  child: widget.bus['image'] != null && (widget.bus['image'] as String).isNotEmpty
                                      ? Image.network(
                                          widget.bus['image'],
                                          height: 180,
                                          width: double.infinity,
                                          fit: BoxFit.cover,
                                        )
                                      : Container(
                                          height: 180,
                                          width: double.infinity,
                                          color: Colors.indigo[50],
                                          child: const Icon(Icons.image, size: 100, color: Colors.grey),
                                        ),
                                ),
                              ),
                              const SizedBox(height: 24),
                              Text(
                                widget.bus['bus_name'] ?? widget.bus['english'] ?? '',
                                style: const TextStyle(
                                  fontSize: 30,
                                  fontWeight: FontWeight.bold,
                                  color: Colors.white,
                                  letterSpacing: 1.2,
                                  shadows: [
                                    Shadow(
                                      color: Colors.black26,
                                      blurRadius: 8,
                                      offset: Offset(0, 2),
                                    ),
                                  ],
                                ),
                                textAlign: TextAlign.center,
                              ),
                              const SizedBox(height: 10),
                              Container(
                                padding: const EdgeInsets.symmetric(horizontal: 18, vertical: 6),
                                decoration: BoxDecoration(
                                  color: Colors.white.withOpacity(0.22),
                                  borderRadius: BorderRadius.circular(16),
                                ),
                                child: Text(
                                  widget.bus['service_type'] != null
                                      ? widget.bus['service_type'].toString().toUpperCase()
                                      : 'N/A',
                                  style: const TextStyle(
                                    fontSize: 16,
                                    color: Color(0xFF4F8AFF),
                                    fontWeight: FontWeight.w600,
                                    letterSpacing: 1.1,
                                  ),
                                ),
                              ),
                              const SizedBox(height: 18),
                            ],
                          ),
                        ),
                      ),
                    ),
                  ),
                  const SizedBox(height: 36),
                  // Stops Section as a bottom sheet style
                  Container(
                    width: double.infinity,
                    padding: const EdgeInsets.symmetric(vertical: 24, horizontal: 20),
                    margin: const EdgeInsets.only(top: 12),
                    decoration: BoxDecoration(
                      gradient: LinearGradient(
                        colors: [
                          Colors.white.withOpacity(0.95),
                          Colors.blue[50]!.withOpacity(0.85),
                        ],
                        begin: Alignment.topLeft,
                        end: Alignment.bottomRight,
                      ),
                      borderRadius: BorderRadius.circular(28),
                      boxShadow: [
                        BoxShadow(
                          color: Colors.blueGrey.withOpacity(0.10),
                          blurRadius: 24,
                          offset: const Offset(0, 10),
                        ),
                      ],
                      border: Border.all(
                        color: Colors.white.withOpacity(0.18),
                        width: 1.2,
                      ),
                    ),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Row(
                          children: const [
                            Icon(Icons.route, color: Color(0xFF4F8AFF), size: 26),
                            SizedBox(width: 10),
                            Text(
                              "Stops",
                              style: TextStyle(
                                fontSize: 24,
                                fontWeight: FontWeight.bold,
                                color: Color(0xFF1A237E),
                                letterSpacing: 1.1,
                                fontFamily: 'Montserrat',
                              ),
                            ),
                          ],
                        ),
                        const SizedBox(height: 4),
                        const Text(
                          "All stops of this bus",
                          style: TextStyle(
                            fontSize: 14,
                            color: Colors.blueGrey,
                            fontWeight: FontWeight.w500,
                            letterSpacing: 0.5,
                          ),
                        ),
                        const SizedBox(height: 14),
                        Divider(
                          color: Colors.blueGrey[100],
                          thickness: 1.1,
                          endIndent: 12,
                          indent: 0,
                        ),
                        const SizedBox(height: 10),
                        stops.isEmpty
                            ? const Center(
                                child: Padding(
                                  padding: EdgeInsets.symmetric(vertical: 32.0),
                                  child: Text(
                                    "No stops available",
                                    style: TextStyle(color: Colors.grey, fontSize: 16),
                                  ),
                                ),
                              )
                            : Column(
                                children: List.generate(
                                  stops.length,
                                  (index) => AnimatedContainer(
                                    duration: Duration(milliseconds: 300 + index * 40),
                                    curve: Curves.easeOutBack,
                                    margin: const EdgeInsets.symmetric(vertical: 6),
                                    child: Material(
                                      color: Colors.transparent,
                                      child: InkWell(
                                        borderRadius: BorderRadius.circular(16),
                                        onTap: () {},
                                        child: Container(
                                          decoration: BoxDecoration(
                                            gradient: LinearGradient(
                                              colors: [
                                                Colors.blue[50]!,
                                                Colors.white.withOpacity(0.85),
                                              ],
                                              begin: Alignment.centerLeft,
                                              end: Alignment.centerRight,
                                            ),
                                            borderRadius: BorderRadius.circular(16),
                                            boxShadow: [
                                              BoxShadow(
                                                color: Colors.blue[100]!.withOpacity(0.18),
                                                blurRadius: 8,
                                                offset: const Offset(0, 2),
                                              ),
                                            ],
                                          ),
                                          child: ListTile(
                                            leading: (widget.bus['service_type'] != null && (widget.bus['service_type'] as String).trim().isNotEmpty)
                                                ? CircleAvatar(
                                                    backgroundColor: Colors.white,
                                                    child: Icon(Icons.directions_bus, color: Color(0xFF4F8AFF)),
                                                  )
                                                : null,
                                            title: Text(
                                              stops[index],
                                              style: const TextStyle(
                                                fontSize: 17,
                                                color: Color(0xFF1A237E),
                                                fontWeight: FontWeight.w600,
                                                fontFamily: 'Montserrat',
                                              ),
                                            ),
                                            trailing: Icon(Icons.arrow_forward_ios_rounded, color: Colors.blue[200], size: 18),
                                            contentPadding: const EdgeInsets.symmetric(horizontal: 8, vertical: 0),
                                          ),
                                        ),
                                      ),
                                    ),
                                  ),
                                ),
                              ),
                      ],
                    ),
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}
