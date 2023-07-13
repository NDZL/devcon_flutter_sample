import 'package:flutter/material.dart';
import 'dart:async';
import 'package:flutter/services.dart';


void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'DevCon2023',
      theme: ThemeData(
        // This is the theme of your application.
        //
        // TRY THIS: Try running your application with "flutter run". You'll see
        // the application has a blue toolbar. Then, without quitting the app,
        // try changing the seedColor in the colorScheme below to Colors.green
        // and then invoke "hot reload" (save your changes or press the "hot
        // reload" button in a Flutter-supported IDE, or press "r" if you used
        // the command line to start the app).
        //
        // Notice that the counter didn't reset back to zero; the application
        // state is not lost during the reload. To reset the state, use hot
        // restart instead.
        //
        // This works for code too, not just values: Most code changes can be
        // tested with just a hot reload.
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.yellow),
        useMaterial3: true,
      ),
      home: const MyHomePage(title: 'Zebra Devcon 2023 - Flutter PoC Home Page'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key, required this.title});

  // This widget is the home page of your application. It is stateful, meaning
  // that it has a State object (defined below) that contains fields that affect
  // how it looks.

  // This class is the configuration for the state. It holds the values (in this
  // case the title) provided by the parent (in this case the App widget) and
  // used by the build method of the State. Fields in a Widget subclass are
  // always marked "final".

  final String title;

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  int _counter = 0;

  //
  static const stream = EventChannel('com.ndzl.dw/ZebraDatawedgeEventChannel');

  late StreamSubscription _streamSubscription;
  String _latest_barcode = "N/A";

  void _startListener() {
    _streamSubscription = stream.receiveBroadcastStream().listen(_listenStream);
  }

  void _cancelListener() {
    _streamSubscription.cancel();
    setState(() {
      _latest_barcode = "";
    });
  }

  void _listenStream(value) {
    debugPrint("Received From Native:  $value\n");
    setState(() {
      _latest_barcode = value;
    });
  }


  //////////
  static const platform = MethodChannel('com.ndzl.dw/ZebraDatawedgeMethodChannel');
  String _methodChannelCallResult = '...';

  Future<void> _softScanTriggerStart() async {
    String callToPlatformMethodResult;
    try {
      final int result = await platform.invokeMethod('softScanTriggerStart');
      callToPlatformMethodResult = 'Platform method call result: $result % .';
    } on PlatformException catch (e) {
      callToPlatformMethodResult = "Failed to get result: '${e.message}'.";
    }

    setState(() {
      _methodChannelCallResult = callToPlatformMethodResult;
    });
  }

  void _incrementCounter() {
    setState(() {
      // This call to setState tells the Flutter framework that something has
      // changed in this State, which causes it to rerun the build method below
      // so that the display can reflect the updated values. If we changed
      // _counter without calling setState(), then the build method would not be
      // called again, and so nothing would appear to happen.
      _counter++;
    });
  }




  @override
  void didChangeDependencies() { //called when the page is created
    _startListener(); //initializing the event channel triggers the broadcast receiver registration on the android platform side
  }

  @override
  Widget build(BuildContext context) {
    return Material(
      child: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.spaceEvenly,
          children: [
            ElevatedButton(
              onPressed: _softScanTriggerStart,
              child: const Text('SOFT SCAN TRIGGER'),
            ),
            Text(_latest_barcode),
          ],
        ),
      ),
    );
  }


}
