#!/bin/sh
tmux new-session -d -s S3-Sync -n vim

tmux send-keys -t S3-Sync:0 'vim .' C-m
tmux split-window -t S3-Sync:0 
tmux send-keys -t S3-Sync:0 'lein repl' C-m

tmux -2 attach-session -t S3-Sync
