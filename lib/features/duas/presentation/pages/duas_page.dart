
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:islami_admin/features/duas/presentation/bloc/duas_bloc.dart';
import 'package:islami_admin/features/duas/presentation/bloc/duas_event.dart';
import 'package:islami_admin/features/duas/presentation/bloc/duas_state.dart';
import 'package:islami_admin/features/home/presentation/widgets/custom_drawer.dart';

class DuasPage extends StatefulWidget {
  const DuasPage({super.key});

  @override
  State<DuasPage> createState() => _DuasPageState();
}

class _DuasPageState extends State<DuasPage> {
  final TextEditingController _controller = TextEditingController();

  @override
  void initState() {
    super.initState();
    context.read<DuasBloc>().add(FetchDuas());
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Duas'),
        actions: [
          IconButton(
            icon: const Icon(Icons.save),
            onPressed: () {
              context.read<DuasBloc>().add(SaveDuasEvent(_controller.text));
            },
          ),
        ],
      ),
      drawer: const CustomDrawer(),
      body: BlocConsumer<DuasBloc, DuasState>(
        listener: (context, state) {
          if (state is DuasLoaded) {
            _controller.text = state.content;
          } else if (state is DuasError) {
            ScaffoldMessenger.of(context).showSnackBar(
              SnackBar(content: Text(state.message)),
            );
          } else if (state is DuasSaved) {
            ScaffoldMessenger.of(context).showSnackBar(
              const SnackBar(content: Text('Duas saved successfully!')),
            );
          }
        },
        builder: (context, state) {
          if (state is DuasLoading) {
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
