import pandas as pd
import numpy as np
import matplotlib.pyplot as plt


# stats_path = "/Users/erotundo/git/UniNotes/ASO/statistics"
stats_path = "../statistics"

runs = ['hetero.1v1',
        'hetero.2v1',
        'hetero.3v1',
        'hetero.3v2.ho',
        'hetero.3v3.ho']

# int generation;
# List<SubpopData> subpopData;
# int corralled;
# int escaped;
# int evaluations;
# double corralledRatio;
# double escapedRatio;
# double popMeanFitness;
# double popBestOfGeneration;
# double popBestSoFar;

# double mean;
# double bestOfGeneration;
# double bestSoFar;

fitness_cols = {
    1: [2],
    2: [2, 5],
    3: [2, 5, 8]
}

for run in runs:
    num_sub_pop = int(run.split('.')[1].split('v')[0])
    print run + "\n"
    df = pd.DataFrame()
    for i in range(1, 41):
        file_path = stats_path + str("/" + str(run) + "/" + str(i)+'.stat')     
        df_run = pd.read_csv(file_path, sep=' ', header=None, index_col=0, skipfooter=1, engine='python')
        dogs_fitness = df_run.iloc[:, fitness_cols[num_sub_pop]].mean(axis=1)
        mean_dog_fitness = dogs_fitness.mean()
        # print mean_dog_fitness
        mean_corralled_ratio = df_run.iloc[:, -5].mean()
        # print mean_corralled_ratio
        mean_escaped_ratio = df_run.iloc[:, -4].mean()
        # print mean_escaped_ratio
        
        df.loc[i, "fitness"] = mean_dog_fitness
        df.loc[i, "corralled_ratio"] = mean_corralled_ratio
        df.loc[i, "escaped_ratio"] = mean_escaped_ratio

    # print df
    print str(df.loc[:, "fitness"].describe()) + "\n"
    print str(df.loc[:, "corralled_ratio"].describe()) + "\n"
    print str(df.loc[:, "escaped_ratio"].describe()) + "\n\n\n"


data1 = dfs[0:3]
data2 = dfs[0] + dfs[3:]
data = [data1, data2]

ind = np.arange(2)
width = 0.35       # the width of the bars
for d in data:
    fig, ax = plt.subplots()
    for df in d:
        corralled = df.loc[:, "corralled_ratio"]
        escaped = df.loc[:, "escaped_ratio"]
        rects1 = ax.bar(ind, [corralled.mean(), escaped.mean()], width, color='r', yerr=[corralled.std(), escaped.std()])

    # add some text for labels, title and axes ticks
    ax.set_ylabel('Ratio')
    ax.set_title('')
    ax.set_xticks(ind + width)
    ax.set_xticklabels(("Corralled", "Escaped"))

    plt.show()
