
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:islami_admin/features/azkar/presentation/bloc/azkar_bloc.dart';
import 'package:islami_admin/features/azkar/presentation/bloc/azkar_event.dart';
import 'package:islami_admin/features/azkar/presentation/bloc/azkar_state.dart';

class AzkarPage extends StatefulWidget {
  const AzkarPage({super.key});

  @override
  State<AzkarPage> createState() => _AzkarPageState();
}

class _AzkarPageState extends State<AzkarPage> {
  final TextEditingController _controller = TextEditingController();

  @override
  void initState() {
    super.initState();
    context.read<AzkarBloc>().add(FetchAzkar());
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Azkar'),
        actions: [
          IconButton(
            icon: const Icon(Icons.save),
            onPressed: () {
              context.read<AzkarBloc>().add(SaveAzkarEvent(_controller.text));
            },
          ),
        ],
      ),
      body: BlocConsumer<AzkarBloc, AzkarState>(
        listener: (context, state) {
          if (state is AzkarLoaded) {
            _controller.text = state.content;
          } else if (state is AzkarError) {
            ScaffoldMessenger.of(context).showSnackBar(
              SnackBar(content: Text(state.message)),
            );
          } else if (state is AzkarSaved) {
            ScaffoldMessenger.of(context).showSnackBar(
              const SnackBar(content: Text('Azkar saved successfully!')),
            );
          }
        },
        builder: (context, state) {
          if (state is AzkarLoading) {
            return const Center(child: CircularProgressIndicator());
          }
          return Padding(
            padding: const EdgeInsets.all(16.0),
            child: TextField(
              controller: _controller,
              maxLines: null,
              decoration: const InputDecoration(
                border: OutlineInputBorder(),
              ),
            ),
          );
        },
      ),
    );
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }
}
